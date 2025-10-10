package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.req.ClienteRequestDto;
import com.nakacorp.backend.dto.req.ClienteUpdateDto;
import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.dto.res.ClienteResponseDto;
import com.nakacorp.backend.dto.res.ClienteSummaryDto;
import com.nakacorp.backend.dto.res.PageResponseDto;
import com.nakacorp.backend.model.enums.StatusLead;
import com.nakacorp.backend.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para gerenciamento de Clientes/Leads do CRM
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Gestão de clientes e leads do CRM")
@SecurityRequirement(name = "bearer-jwt")
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Listar clientes", description = "Lista todos os clientes com paginação e ordenação")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ClienteResponseDto>>> findAll(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "nome") String sortBy,
            @Parameter(description = "Direção da ordenação (asc/desc)") @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ClienteResponseDto> result = clienteService.findAll(pageable);
        PageResponseDto<ClienteResponseDto> response = PageResponseDto.fromPage(result);

        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID", description = "Retorna os dados completos de um cliente específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ClienteResponseDto>> findById(
            @Parameter(description = "ID do cliente") @PathVariable Long id) {

        return clienteService.findById(id)
                .map(cliente -> ResponseEntity.ok(ApiResponseDto.success(cliente)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("Cliente não encontrado")));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Buscar por status", description = "Lista clientes filtrados por status do lead")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<ClienteSummaryDto>>> findByStatus(
            @Parameter(description = "Status do lead") @PathVariable StatusLead status) {

        List<ClienteSummaryDto> clientes = clienteService.findByStatus(status);
        return ResponseEntity.ok(ApiResponseDto.success(clientes));
    }

    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cadastra um novo cliente/lead no sistema")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ClienteResponseDto>> create(
            @Parameter(description = "Dados do novo cliente") @RequestBody @Valid ClienteRequestDto request) {

        try {
            ClienteResponseDto cliente = clienteService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Cliente criado com sucesso", cliente));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ClienteResponseDto>> update(
            @Parameter(description = "ID do cliente") @PathVariable Long id,
            @Parameter(description = "Novos dados do cliente") @RequestBody @Valid ClienteUpdateDto request) {

        try {
            ClienteResponseDto cliente = clienteService.update(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Cliente atualizado com sucesso", cliente));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status", description = "Atualiza o status do lead de um cliente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ClienteResponseDto>> updateStatus(
            @Parameter(description = "ID do cliente") @PathVariable Long id,
            @Parameter(description = "Novo status do lead") @RequestParam StatusLead novoStatus) {

        try {
            ClienteResponseDto cliente = clienteService.updateStatus(id, novoStatus);
            return ResponseEntity.ok(ApiResponseDto.success("Status atualizado com sucesso", cliente));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Remove um cliente do sistema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @Parameter(description = "ID do cliente") @PathVariable Long id) {

        try {
            clienteService.delete(id);
            return ResponseEntity.ok(ApiResponseDto.success("Cliente excluído com sucesso", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }
}