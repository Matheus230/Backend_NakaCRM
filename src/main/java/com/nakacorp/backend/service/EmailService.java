package com.nakacorp.backend.service;

import com.nakacorp.backend.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final InteracaoClienteService interacaoService;

    @Value("${spring.mail.username:noreply@empresa.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender, InteracaoClienteService interacaoService) {
        this.mailSender = mailSender;
        this.interacaoService = interacaoService;
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
                        fromEmail,
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
                        fromEmail,
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
                        fromEmail,
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
            message.setFrom(fromEmail);
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

            helper.setFrom(fromEmail);
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
