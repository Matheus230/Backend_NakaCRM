package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.req.InteracaoClienteRequestDto;
import com.nakacorp.backend.dto.req.InteracaoClienteUpdateDto;
import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.dto.res.InteracaoClienteResponseDto;
import com.nakacorp.backend.dto.res.InteracaoStatsDto;
import com.nakacorp.backend.dto.res.PageResponseDto;
import com.nakacorp.backend.dto.res.TimelineClienteDto;
import com.nakacorp.backend.model.enums.TipoInteracao;
import com.nakacorp.backend.service.InteracaoClienteService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST para gerenciamento de Interações com Clientes
 * <p>
 * Gerencia o histórico completo de interações entre a equipe e os clientes,
 * incluindo emails, ligações telefônicas, reuniões, whatsapp e outros tipos de contato.
 * Fornece timeline de atividades por cliente, estatísticas de interações,
 * filtros por tipo, período e usuário. Essencial para rastreabilidade e follow-up.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/interacoes")
@Tag(name = "Interações", description = "Gestão da timeline de interações com clientes")
@SecurityRequirement(name = "bearer-jwt")
@CrossOrigin(origins = "*")
public class InteracaoClienteController {

    private final InteracaoClienteService interacaoService;

    /**
     * Construtor com injeção de dependência do serviço de interações.
     *
     * @param interacaoService serviço de gerenciamento de interações com clientes
     */
    @Autowired
    public InteracaoClienteController(InteracaoClienteService interacaoService) {
        this.interacaoService = interacaoService;
    }

    @GetMapping
    @Operation(summary = "Listar interações", description = "Lista todas as interações com paginação")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<InteracaoClienteResponseDto>>> findAll(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<InteracaoClienteResponseDto> result = interacaoService.findAll(pageable);
        PageResponseDto<InteracaoClienteResponseDto> response = PageResponseDto.fromPage(result);

        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/cliente/{clienteId}/timeline")
    @Operation(summary = "Timeline do cliente", description = "Retorna o histórico completo de interações de um cliente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<TimelineClienteDto>>> getTimelineCliente(
            @Parameter(description = "ID do cliente") @PathVariable Long clienteId) {

        List<TimelineClienteDto> timeline = interacaoService.getTimelineCliente(clienteId);
        return ResponseEntity.ok(ApiResponseDto.success(timeline));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Interações por usuário", description = "Lista interações realizadas por um usuário específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<InteracaoClienteResponseDto>>> findByUsuario(
            @Parameter(description = "ID do usuário") @PathVariable Long usuarioId) {

        List<InteracaoClienteResponseDto> interacoes = interacaoService.findByUsuario(usuarioId);
        return ResponseEntity.ok(ApiResponseDto.success(interacoes));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Interações por tipo", description = "Lista interações de um tipo específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<InteracaoClienteResponseDto>>> findByTipo(
            @Parameter(description = "Tipo da interação") @PathVariable TipoInteracao tipo) {

        List<InteracaoClienteResponseDto> interacoes = interacaoService.findByTipo(tipo);
        return ResponseEntity.ok(ApiResponseDto.success(interacoes));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Interações por período", description = "Lista interações dentro de um período específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<InteracaoClienteResponseDto>>> findByPeriodo(
            @Parameter(description = "Data de início")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data de fim")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        List<InteracaoClienteResponseDto> interacoes = interacaoService.findByPeriodo(inicio, fim);
        return ResponseEntity.ok(ApiResponseDto.success(interacoes));
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas de interações", description = "Retorna estatísticas gerais das interações")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<InteracaoStatsDto>> getStats() {
        InteracaoStatsDto stats = interacaoService.getStats();
        return ResponseEntity.ok(ApiResponseDto.success(stats));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar interação por ID", description = "Retorna os dados de uma interação específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<InteracaoClienteResponseDto>> findById(
            @Parameter(description = "ID da interação") @PathVariable Long id) {

        return interacaoService.findById(id)
                .map(interacao -> ResponseEntity.ok(ApiResponseDto.success(interacao)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("Interação não encontrada")));
    }

    @PostMapping
    @Operation(summary = "Criar interação", description = "Registra uma nova interação com cliente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<InteracaoClienteResponseDto>> create(
            @Parameter(description = "Dados da interação") @RequestBody @Valid InteracaoClienteRequestDto request) {

        try {
            InteracaoClienteResponseDto interacao = interacaoService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Interação registrada com sucesso", interacao));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PostMapping("/email")
    @Operation(summary = "Registrar email", description = "Registra automaticamente uma interação de email")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Void>> registrarEmail(
            @Parameter(description = "ID do cliente") @RequestParam Long clienteId,
            @Parameter(description = "Assunto do email") @RequestParam String assunto,
            @Parameter(description = "Remetente") @RequestParam String remetente,
            @Parameter(description = "ID do usuário") @RequestParam(required = false) Long usuarioId) {

        try {
            interacaoService.registrarEmail(clienteId, assunto, remetente, usuarioId);
            return ResponseEntity.ok(ApiResponseDto.success("Email registrado com sucesso", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PostMapping("/telefone")
    @Operation(summary = "Registrar ligação", description = "Registra automaticamente uma interação de telefone")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Void>> registrarTelefone(
            @Parameter(description = "ID do cliente") @RequestParam Long clienteId,
            @Parameter(description = "Número do telefone") @RequestParam String numeroTelefone,
            @Parameter(description = "Duração da ligação") @RequestParam String duracao,
            @Parameter(description = "ID do usuário") @RequestParam(required = false) Long usuarioId) {

        try {
            interacaoService.registrarTelefone(clienteId, numeroTelefone, duracao, usuarioId);
            return ResponseEntity.ok(ApiResponseDto.success("Ligação registrada com sucesso", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar interação", description = "Atualiza os dados de uma interação existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<InteracaoClienteResponseDto>> update(
            @Parameter(description = "ID da interação") @PathVariable Long id,
            @Parameter(description = "Novos dados da interação") @RequestBody @Valid InteracaoClienteUpdateDto request) {

        try {
            InteracaoClienteResponseDto interacao = interacaoService.update(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Interação atualizada com sucesso", interacao));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir interação", description = "Remove uma interação do sistema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @Parameter(description = "ID da interação") @PathVariable Long id) {

        try {
            interacaoService.delete(id);
            return ResponseEntity.ok(ApiResponseDto.success("Interação excluída com sucesso", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }
}
