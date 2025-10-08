package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.service.ClienteService;
import com.nakacorp.backend.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de Envio de Emails
 * <p>
 * Permite envio de emails individuais e em massa (broadcast) para clientes.
 * Suporta diferentes tipos de emails: simples (texto), boas-vindas, follow-up e promocionais.
 * Todos os emails utilizam templates HTML profissionais e são enviados de forma assíncrona.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/emails")
@Tag(name = "Emails", description = "Gestão de envio de emails para clientes")
@SecurityRequirement(name = "bearer-jwt")
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;
    private final ClienteService clienteService;

    /**
     * Construtor com injeção de dependências dos serviços necessários.
     *
     * @param emailService serviço de envio de emails
     * @param clienteService serviço de gerenciamento de clientes
     */
    @Autowired
    public EmailController(EmailService emailService, ClienteService clienteService) {
        this.emailService = emailService;
        this.clienteService = clienteService;
    }

    @PostMapping("/enviar-simples")
    @Operation(summary = "Enviar email simples", description = "Envia um email de texto simples")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Void>> enviarEmailSimples(
            @Parameter(description = "Email do destinatário") @RequestParam @Email String destinatario,
            @Parameter(description = "Assunto do email") @RequestParam @NotBlank String assunto,
            @Parameter(description = "Conteúdo do email") @RequestParam @NotBlank String mensagem) {

        try {
            emailService.enviarEmailSimples(destinatario, assunto, mensagem);
            return ResponseEntity.ok(ApiResponseDto.success("Email enviado com sucesso", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Erro ao enviar email: " + e.getMessage()));
        }
    }

    @PostMapping("/broadcast/boas-vindas")
    @Operation(summary = "Broadcast boas-vindas", description = "Envia emails de boas-vindas para múltiplos clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> broadcastBoasVindas(
            @Parameter(description = "Lista de IDs dos clientes") @RequestBody @Valid List<Long> clienteIds) {

        try {
            for (Long clienteId : clienteIds) {
                clienteService.findById(clienteId).ifPresent(clienteDto -> {
                    Cliente cliente = convertDtoToEntity(clienteDto);
                    emailService.enviarEmailBoasVindas(cliente);
                });
            }
            return ResponseEntity.ok(ApiResponseDto.success(
                    String.format("Emails de boas-vindas agendados para %d clientes", clienteIds.size()), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Erro no envio em massa: " + e.getMessage()));
        }
    }

    @PostMapping("/broadcast/follow-up")
    @Operation(summary = "Broadcast follow-up", description = "Envia emails de follow-up para múltiplos clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> broadcastFollowUp(
            @Parameter(description = "Lista de IDs dos clientes") @RequestBody @Valid List<Long> clienteIds,
            @Parameter(description = "Mensagem personalizada") @RequestParam @NotBlank String mensagemPersonalizada) {

        try {
            for (Long clienteId : clienteIds) {
                clienteService.findById(clienteId).ifPresent(clienteDto -> {
                    Cliente cliente = convertDtoToEntity(clienteDto);
                    emailService.enviarEmailFollowUp(cliente, mensagemPersonalizada);
                });
            }
            return ResponseEntity.ok(ApiResponseDto.success(
                    String.format("Emails de follow-up agendados para %d clientes", clienteIds.size()), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Erro no envio em massa: " + e.getMessage()));
        }
    }

    @PostMapping("/cliente/{clienteId}/boas-vindas")
    @Operation(summary = "Enviar email de boas-vindas", description = "Envia email de boas-vindas para um cliente específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Void>> enviarBoasVindas(
            @Parameter(description = "ID do cliente") @PathVariable Long clienteId) {

        return clienteService.findById(clienteId)
                .map(clienteDto -> {
                    Cliente cliente = convertDtoToEntity(clienteDto);
                    emailService.enviarEmailBoasVindas(cliente);
                    return ResponseEntity.<ApiResponseDto<Void>>ok(ApiResponseDto.success("Email de boas-vindas enviado com sucesso", null));
                })
                .orElse(ResponseEntity.status(404)
                        .body(ApiResponseDto.<Void>error("Cliente não encontrado")));
    }

    @PostMapping("/cliente/{clienteId}/follow-up")
    @Operation(summary = "Enviar email de follow-up", description = "Envia email de follow-up para um cliente específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Void>> enviarFollowUp(
            @Parameter(description = "ID do cliente") @PathVariable Long clienteId,
            @Parameter(description = "Mensagem personalizada") @RequestParam @NotBlank String mensagemPersonalizada) {

        return clienteService.findById(clienteId)
                .map(clienteDto -> {
                    Cliente cliente = convertDtoToEntity(clienteDto);
                    emailService.enviarEmailFollowUp(cliente, mensagemPersonalizada);
                    return ResponseEntity.<ApiResponseDto<Void>>ok(ApiResponseDto.success("Email de follow-up enviado com sucesso", null));
                })
                .orElse(ResponseEntity.status(404)
                        .body(ApiResponseDto.<Void>error("Cliente não encontrado")));
    }

    @PostMapping("/cliente/{clienteId}/promocional")
    @Operation(summary = "Enviar email promocional", description = "Envia email promocional para um cliente específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Void>> enviarPromocional(
            @Parameter(description = "ID do cliente") @PathVariable Long clienteId,
            @Parameter(description = "Título do produto/promoção") @RequestParam @NotBlank String tituloProduto,
            @Parameter(description = "Descrição da promoção") @RequestParam @NotBlank String descricao) {

        return clienteService.findById(clienteId)
                .map(clienteDto -> {
                    Cliente cliente = convertDtoToEntity(clienteDto);
                    emailService.enviarEmailPromocional(cliente, tituloProduto, descricao);
                    return ResponseEntity.<ApiResponseDto<Void>>ok(ApiResponseDto.success("Email promocional enviado com sucesso", null));
                })
                .orElse(ResponseEntity.status(404)
                        .body(ApiResponseDto.<Void>error("Cliente não encontrado")));
    }

    private Cliente convertDtoToEntity(com.nakacorp.backend.dto.res.ClienteResponseDto clienteDto) {
        Cliente cliente = new Cliente();
        cliente.setId(clienteDto.id());
        cliente.setNome(clienteDto.nome());
        cliente.setEmail(clienteDto.email());
        cliente.setTelefone(clienteDto.telefone());
        cliente.setEmpresa(clienteDto.empresa());
        cliente.setOrigemLead(clienteDto.origemLead());
        cliente.setStatusLead(clienteDto.statusLead());
        return cliente;
    }
}
