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

@RestController
@RequestMapping("/emails")
@Tag(name = "Emails", description = "Gestão de envio de emails para clientes")
@SecurityRequirement(name = "bearer-jwt")
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailService emailService;
    private final ClienteService clienteService;

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
