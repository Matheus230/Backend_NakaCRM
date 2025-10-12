package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.req.LeadCaptureRequestDto;
import com.nakacorp.backend.dto.res.LeadCaptureResponseDto;
import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.ClienteInteresse;
import com.nakacorp.backend.model.Produto;
import com.nakacorp.backend.model.enums.NivelInteresse;
import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;
import com.nakacorp.backend.repository.ClienteRepository;
import com.nakacorp.backend.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Serviço responsável pela captação de leads via formulário público.
 * <p>
 * Este serviço gerencia o processo completo de captação de leads:
 * - Validação dos dados
 * - Criação ou atualização do cliente
 * - Registro do interesse no produto
 * - Envio de email de confirmação
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 2025-01-12
 */
@Service
public class LeadCaptureService {

    private static final Logger logger = LoggerFactory.getLogger(LeadCaptureService.class);

    private final ClienteRepository clienteRepository;
    private final EmailService emailService;

    @Value("${app.company.name:NakaCorp}")
    private String companyName;

    @Value("${app.company.address:}")
    private String companyAddress;

    @Value("${app.company.phone:(11) 1234-5678}")
    private String companyPhone;

    @Value("${app.company.email:contato@nakacorp.com}")
    private String companyEmail;

    @Autowired
    public LeadCaptureService(
            ClienteRepository clienteRepository,
            EmailService emailService
    ) {
        this.clienteRepository = clienteRepository;
        this.emailService = emailService;
    }

    /**
     * Captura um novo lead e registra seu interesse em um produto.
     *
     * @param request Dados do lead capturado
     * @return DTO com informações de confirmação
     * @throws IllegalArgumentException se o produto não existir
     */
    @Transactional
    public LeadCaptureResponseDto capturarLead(LeadCaptureRequestDto request) {
        logger.info("Iniciando captação de lead: {}", request.email());

        Optional<Cliente> clienteExistente = clienteRepository.findByEmail(request.email());

        Cliente cliente;
        boolean isNovoCliente;

        if (clienteExistente.isPresent()) {
            cliente = clienteExistente.get();
            isNovoCliente = false;

            logger.info("Cliente já existe, atualizando informações: {}", cliente.getEmail());

            if (request.nome() != null && !request.nome().isEmpty()) {
                cliente.setNome(request.nome());
            }
            if (request.telefone() != null && !request.telefone().isEmpty()) {
                cliente.setTelefone(request.telefone());
            }
            if (request.empresa() != null && !request.empresa().isEmpty()) {
                cliente.setEmpresa(request.empresa());
            }
            if (request.cargo() != null && !request.cargo().isEmpty()) {
                cliente.setCargo(request.cargo());
            }

            cliente.setDataUltimaInteracao(LocalDateTime.now());

        } else {
            isNovoCliente = true;

            logger.info("Criando novo cliente: {}", request.email());

            cliente = Cliente.builder()
                    .nome(request.nome())
                    .email(request.email())
                    .telefone(request.telefone())
                    .empresa(request.empresa())
                    .cargo(request.cargo())
                    .origemLead(OrigemLead.LANDING_PAGE)
                    .statusLead(StatusLead.NOVO)
                    .dataPrimeiroContato(LocalDateTime.now())
                    .dataUltimaInteracao(LocalDateTime.now())
                    .observacoes(construirObservacoes(request))
                    .interesses(new ArrayList<>())
                    .build();
        }

        cliente = clienteRepository.save(cliente);

        if (cliente.getInteresses() == null) {
            cliente.setInteresses(new ArrayList<>());
        }

        clienteRepository.save(cliente);

        enviarEmailConfirmacao(cliente, request.formaContatoPreferida());

        return LeadCaptureResponseDto.from(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
        );
    }

    /**
     * Constrói observações baseadas nos dados do formulário
     */
    private String construirObservacoes(LeadCaptureRequestDto request) {
        StringBuilder obs = new StringBuilder();
        obs.append("Lead capturado via formulário público.\n");
        obs.append("Preferência de contato: ").append(request.formaContatoPreferida()).append("\n");

        if (request.observacoes() != null && !request.observacoes().isEmpty()) {
            obs.append("Observações do lead: ").append(request.observacoes());
        }

        return obs.toString();
    }

    /**
     * Envia email de confirmação para o lead
     */
    private void enviarEmailConfirmacao(Cliente cliente, String formaContatoPreferida) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", cliente.getNome());
            variables.put("customerEmail", cliente.getEmail());
            variables.put("customerPhone", cliente.getTelefone());
            variables.put("contactPreference", formatarFormaContato(formaContatoPreferida));
            variables.put("companyName", companyName);
            variables.put("companyAddress", companyAddress != null && !companyAddress.isEmpty()
                    ? companyAddress
                    : "Endereço não disponível");
            variables.put("companyPhone", companyPhone);
            variables.put("companyEmail", companyEmail);
            variables.put("currentYear", Year.now().getValue());

            String htmlContent = loadTemplate("lead-confirmation.html", variables);
            String assunto = String.format("Confirmação de Interesse");

            emailService.enviarEmailHtml(cliente.getEmail(), assunto, htmlContent);

            logger.info("Email de confirmação enviado para: {}", cliente.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao enviar email de confirmação para: {}", cliente.getEmail(), e);
        }
    }

    /**
     * Formata a forma de contato para exibição no email
     */
    private String formatarFormaContato(String formaContato) {
        return switch (formaContato.toUpperCase()) {
            case "EMAIL" -> "📧 Email";
            case "TELEFONE" -> "📞 Telefone";
            case "WHATSAPP" -> "💬 WhatsApp";
            default -> formaContato;
        };
    }

    /**
     * Carrega template HTML e substitui variáveis
     */
    private String loadTemplate(String templateName, Map<String, Object> variables) throws IOException {
        String templatePath = String.format("src/main/resources/templates/email/%s", templateName);
        String template = new String(Files.readAllBytes(Paths.get(templatePath)), StandardCharsets.UTF_8);

        // Substituir variáveis no template
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            template = template.replace(placeholder, value);
        }

        return template;
    }
}
