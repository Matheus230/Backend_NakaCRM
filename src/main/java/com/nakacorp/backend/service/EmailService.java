package com.nakacorp.backend.service;

import com.nakacorp.backend.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço para envio de emails utilizando templates HTML
 * Suporta envio de emails de boas-vindas, follow-up, promocionais e cobranças
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final InteracaoClienteService interacaoService;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.company.name:NakaCorp}")
    private String companyName;

    @Value("${app.company.address:Rua Exemplo, 123 - São Paulo/SP}")
    private String companyAddress;

    @Value("${app.company.phone:(11) 1234-5678}")
    private String companyPhone;

    @Value("${app.company.email:contato@nakacorp.com}")
    private String companyEmail;

    /**
     * Retorna o email que deve ser usado como remetente.
     * Usa o spring.mail.username se configurado, caso contrário usa o email da empresa.
     */
    private String getFromEmail() {
        return (mailUsername != null && !mailUsername.isEmpty()) ? mailUsername : companyEmail;
    }

    @Autowired
    public EmailService(JavaMailSender mailSender, InteracaoClienteService interacaoService) {
        this.mailSender = mailSender;
        this.interacaoService = interacaoService;
    }

    /**
     * Envia email de lembrete de pagamento usando template HTML
     *
     * @param destinatario Email do destinatário
     * @param nomeCliente Nome do cliente
     * @param numeroFatura Número da fatura
     * @param dataVencimento Data de vencimento
     * @param descricao Descrição da fatura
     * @param valor Valor da fatura
     * @param linkPagamento Link para realizar o pagamento
     * @return CompletableFuture para execução assíncrona
     */
    public CompletableFuture<Void> enviarEmailLembreteCobranca(
            String destinatario,
            String nomeCliente,
            String numeroFatura,
            String dataVencimento,
            String descricao,
            BigDecimal valor,
            String linkPagamento
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("customerName", nomeCliente);
                variables.put("invoiceNumber", numeroFatura);
                variables.put("dueDate", dataVencimento);
                variables.put("description", descricao);
                variables.put("amount", String.format("%.2f", valor));
                variables.put("paymentLink", linkPagamento);
                variables.put("companyName", companyName);
                variables.put("companyAddress", companyAddress);
                variables.put("companyPhone", companyPhone);
                variables.put("companyEmail", companyEmail);

                String htmlContent = loadTemplate("payment-reminder.html", variables);
                String assunto = "Lembrete: Pagamento a vencer - Fatura #" + numeroFatura;

                enviarEmailHtml(destinatario, assunto, htmlContent);
                logger.info("Email de lembrete de cobrança enviado para: {}", destinatario);
            } catch (Exception e) {
                logger.error("Erro ao enviar email de lembrete de cobrança para: {}", destinatario, e);
                throw new RuntimeException("Falha ao enviar email de cobrança", e);
            }
        });
    }

    /**
     * Envia email de pagamento vencido usando template HTML
     *
     * @param destinatario Email do destinatário
     * @param nomeCliente Nome do cliente
     * @param numeroFatura Número da fatura
     * @param dataVencimento Data de vencimento
     * @param diasAtraso Dias em atraso
     * @param descricao Descrição da fatura
     * @param valorOriginal Valor original da fatura
     * @param taxaMulta Taxa de multa em porcentagem
     * @param valorMulta Valor da multa
     * @param valorJuros Valor dos juros
     * @param valorTotal Valor total atualizado
     * @param linkPagamento Link para realizar o pagamento
     * @return CompletableFuture para execução assíncrona
     */
    public CompletableFuture<Void> enviarEmailCobrancaVencida(
            String destinatario,
            String nomeCliente,
            String numeroFatura,
            String dataVencimento,
            long diasAtraso,
            String descricao,
            BigDecimal valorOriginal,
            BigDecimal taxaMulta,
            BigDecimal valorMulta,
            BigDecimal valorJuros,
            BigDecimal valorTotal,
            String linkPagamento
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("customerName", nomeCliente);
                variables.put("invoiceNumber", numeroFatura);
                variables.put("dueDate", dataVencimento);
                variables.put("daysOverdue", diasAtraso);
                variables.put("description", descricao);
                variables.put("originalAmount", String.format("%.2f", valorOriginal));
                variables.put("penaltyRate", String.format("%.1f", taxaMulta));
                variables.put("penaltyAmount", String.format("%.2f", valorMulta));
                variables.put("interestAmount", String.format("%.2f", valorJuros));
                variables.put("totalAmount", String.format("%.2f", valorTotal));
                variables.put("paymentLink", linkPagamento);
                variables.put("companyName", companyName);
                variables.put("companyAddress", companyAddress);
                variables.put("companyPhone", companyPhone);
                variables.put("companyEmail", companyEmail);

                String htmlContent = loadTemplate("overdue-payment.html", variables);
                String assunto = "URGENTE: Pagamento Vencido - Fatura #" + numeroFatura;

                enviarEmailHtml(destinatario, assunto, htmlContent);
                logger.info("Email de cobrança vencida enviado para: {}", destinatario);
            } catch (Exception e) {
                logger.error("Erro ao enviar email de cobrança vencida para: {}", destinatario, e);
                throw new RuntimeException("Falha ao enviar email de cobrança vencida", e);
            }
        });
    }

    /**
     * Envia email de confirmação de pagamento usando template HTML
     *
     * @param destinatario Email do destinatário
     * @param nomeCliente Nome do cliente
     * @param numeroFatura Número da fatura
     * @param dataPagamento Data do pagamento
     * @param metodoPagamento Método de pagamento utilizado
     * @param descricao Descrição da fatura
     * @param valor Valor pago
     * @param codigoConfirmacao Código de confirmação do pagamento
     * @param linkRecibo Link para download do recibo
     * @return CompletableFuture para execução assíncrona
     */
    public CompletableFuture<Void> enviarEmailConfirmacaoPagamento(
            String destinatario,
            String nomeCliente,
            String numeroFatura,
            String dataPagamento,
            String metodoPagamento,
            String descricao,
            BigDecimal valor,
            String codigoConfirmacao,
            String linkRecibo
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("customerName", nomeCliente);
                variables.put("invoiceNumber", numeroFatura);
                variables.put("paymentDate", dataPagamento);
                variables.put("paymentMethod", metodoPagamento);
                variables.put("description", descricao);
                variables.put("amount", String.format("%.2f", valor));
                variables.put("confirmationCode", codigoConfirmacao);
                variables.put("receiptLink", linkRecibo);
                variables.put("companyName", companyName);
                variables.put("companyAddress", companyAddress);
                variables.put("companyPhone", companyPhone);
                variables.put("companyEmail", companyEmail);

                String htmlContent = loadTemplate("payment-confirmation.html", variables);
                String assunto = "Pagamento Confirmado - Fatura #" + numeroFatura;

                enviarEmailHtml(destinatario, assunto, htmlContent);
                logger.info("Email de confirmação de pagamento enviado para: {}", destinatario);
            } catch (Exception e) {
                logger.error("Erro ao enviar email de confirmação de pagamento para: {}", destinatario, e);
                throw new RuntimeException("Falha ao enviar email de confirmação", e);
            }
        });
    }

    /**
     * Carrega template HTML e substitui variáveis
     *
     * @param templateName Nome do arquivo de template
     * @param variables Map com as variáveis a serem substituídas
     * @return HTML processado
     */
    private String loadTemplate(String templateName, Map<String, Object> variables) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/email/" + templateName);
        String template = new String(Files.readAllBytes(Paths.get(resource.getURI())), StandardCharsets.UTF_8);

        // Substituir variáveis no template
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            template = template.replace(placeholder, value);
        }

        return template;
    }

    public CompletableFuture<Void> enviarEmailBoasVindas(Cliente cliente) {
        return CompletableFuture.runAsync(() -> {
            try {
                String assunto = "Bem-vindo(a) ao nosso CRM!";
                String corpo = construirEmailBoasVindas(cliente);

                enviarEmailHtml(cliente.getEmail(), assunto, corpo);

                interacaoService.registrarEmail(
                        cliente.getId(),
                        assunto,
                        getFromEmail(),
                        null
                );

                logger.info("Email de boas-vindas enviado para: {}", cliente.getEmail());
            } catch (Exception e) {
                logger.error("Erro ao enviar email de boas-vindas para: {}", cliente.getEmail(), e);
            }
        });
    }

    public CompletableFuture<Void> enviarEmailFollowUp(Cliente cliente, String mensagemPersonalizada) {
        return CompletableFuture.runAsync(() -> {
            try {
                String assunto = "Acompanhamento - " + cliente.getNome();
                String corpo = construirEmailFollowUp(cliente, mensagemPersonalizada);

                enviarEmailHtml(cliente.getEmail(), assunto, corpo);

                interacaoService.registrarEmail(
                        cliente.getId(),
                        assunto,
                        getFromEmail(),
                        null
                );

                logger.info("Email de follow-up enviado para: {}", cliente.getEmail());
            } catch (Exception e) {
                logger.error("Erro ao enviar email de follow-up para: {}", cliente.getEmail(), e);
            }
        });
    }

    public CompletableFuture<Void> enviarEmailPromocional(Cliente cliente, String tituloProduto, String descricao) {
        return CompletableFuture.runAsync(() -> {
            try {
                String assunto = "Oferta especial: " + tituloProduto;
                String corpo = construirEmailPromocional(cliente, tituloProduto, descricao);

                enviarEmailHtml(cliente.getEmail(), assunto, corpo);

                interacaoService.registrarEmail(
                        cliente.getId(),
                        assunto,
                        getFromEmail(),
                        null
                );

                logger.info("Email promocional enviado para: {}", cliente.getEmail());
            } catch (Exception e) {
                logger.error("Erro ao enviar email promocional para: {}", cliente.getEmail(), e);
            }
        });
    }

    public void enviarEmailSimples(String destinatario, String assunto, String mensagem) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(getFromEmail());
            message.setTo(destinatario);
            message.setSubject(assunto);
            message.setText(mensagem);

            mailSender.send(message);
            logger.info("Email simples enviado para: {}", destinatario);
        } catch (Exception e) {
            logger.error("Erro ao enviar email simples para: {}", destinatario, e);
            throw new RuntimeException("Falha ao enviar email", e);
        }
    }

    public void enviarEmailHtml(String destinatario, String assunto, String corpoHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(getFromEmail());
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);

            mailSender.send(message);
            logger.info("Email HTML enviado para: {}", destinatario);
        } catch (MessagingException e) {
            logger.error("Erro ao enviar email HTML para: {}", destinatario, e);
            throw new RuntimeException("Falha ao enviar email HTML", e);
        }
    }

    private String construirEmailBoasVindas(Cliente cliente) {
        //SUBSTITUIR PARA TEMPLATE NEXT UPDATE
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Olá, %s!</h2>

                    <p>Seja muito bem-vindo(a) ao nosso sistema CRM!</p>

                    <p>Ficamos felizes em tê-lo(a) conosco. Nossa equipe está pronta para
                    oferecer o melhor atendimento e soluções personalizadas para suas necessidades.</p>

                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #495057;">Seus dados:</h3>
                        <p><strong>Nome:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        %s
                    </div>

                    <p>Em breve, nossa equipe entrará em contato para apresentar nossas soluções.</p>

                    <p style="margin-top: 30px;">
                        Atenciosamente,<br>
                        <strong>Equipe CRM</strong>
                    </p>

                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #dee2e6;">
                    <p style="font-size: 12px; color: #6c757d;">
                        Este email foi enviado automaticamente em %s
                    </p>
                </div>
            </body>
            </html>
            """,
                cliente.getNome(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getEmpresa() != null ?
                        String.format("<p><strong>Empresa:</strong> %s</p>", cliente.getEmpresa()) : "",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }

    private String construirEmailFollowUp(Cliente cliente, String mensagemPersonalizada) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Olá, %s!</h2>

                    <p>Esperamos que você esteja bem!</p>

                    <p>%s</p>

                    <div style="background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #2196f3;">
                        <p style="margin: 0;"><strong>Nossa equipe está sempre disponível para ajudá-lo(a).</strong></p>
                        <p style="margin: 10px 0 0 0;">Responda este email ou entre em contato conosco para esclarecer qualquer dúvida.</p>
                    </div>

                    <p style="margin-top: 30px;">
                        Atenciosamente,<br>
                        <strong>Equipe CRM</strong>
                    </p>

                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #dee2e6;">
                    <p style="font-size: 12px; color: #6c757d;">
                        Email de acompanhamento enviado em %s
                    </p>
                </div>
            </body>
            </html>
            """,
                cliente.getNome(),
                mensagemPersonalizada,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
    }

    private String construirEmailPromocional(Cliente cliente, String tituloProduto, String descricao) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px;">
                        <h1 style="margin: 0; font-size: 28px;">🎉 Oferta Especial!</h1>
                        <p style="margin: 10px 0 0 0; font-size: 18px; opacity: 0.9;">Exclusiva para você, %s</p>
                    </div>

                    <div style="background-color: #f8f9fa; padding: 25px; border-radius: 10px; margin: 20px 0;">
                        <h2 style="margin-top: 0; color: #2c3e50;">%s</h2>
                        <p style="font-size: 16px; color: #495057;">%s</p>
                    </div>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #28a745; color: white; padding: 15px 30px; text-decoration: none; border-radius: 25px; font-weight: bold; display: inline-block;">
                            Ver Detalhes da Oferta
                        </a>
                    </div>

                    <p style="color: #dc3545; font-weight: bold; text-align: center;">
                        ⏰ Oferta por tempo limitado!
                    </p>

                    <p style="margin-top: 30px;">
                        Não perca essa oportunidade única!<br>
                        <strong>Equipe CRM</strong>
                    </p>

                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #dee2e6;">
                    <p style="font-size: 12px; color: #6c757d; text-align: center;">
                        Email promocional enviado em %s<br>
                        Se não deseja mais receber esses emails, <a href="%s/unsubscribe">clique aqui</a>
                    </p>
                </div>
            </body>
            </html>
            """,
                cliente.getNome(),
                tituloProduto,
                descricao,
                baseUrl + "/produtos",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                baseUrl
        );
    }
}
