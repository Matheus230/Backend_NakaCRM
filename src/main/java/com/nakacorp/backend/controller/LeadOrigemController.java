package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.req.LeadOrigemRequestDto;
import com.nakacorp.backend.dto.req.LeadOrigemUpdateDto;
import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.dto.res.LeadOrigemResponseDto;
import com.nakacorp.backend.dto.res.PageResponseDto;
import com.nakacorp.backend.dto.res.UtmAnalyticsDto;
import com.nakacorp.backend.service.LeadOrigemService;
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
 * Controller REST para gerenciamento de Origens de Leads e Tracking UTM
 * <p>
 * Gerencia as origens dos leads capturados pelo sistema, incluindo rastreamento
 * completo de parâmetros UTM (source, medium, campaign, term, content).
 * Fornece analytics detalhado de performance por origem, identificação de canais
 * mais efetivos e análise de ROI de campanhas de marketing.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/lead-origens")
@Tag(name = "Lead Origens", description = "Gestão de origens e tracking UTM dos leads")
@SecurityRequirement(name = "bearer-jwt")
public class LeadOrigemController {

    private final LeadOrigemService leadOrigemService;

    /**
     * Construtor com injeção de dependência do serviço de origens de leads.
     *
     * @param leadOrigemService serviço de gerenciamento de origens e tracking UTM
     */
    @Autowired
    public LeadOrigemController(LeadOrigemService leadOrigemService) {
        this.leadOrigemService = leadOrigemService;
    }

    @GetMapping
    @Operation(summary = "Listar origens de leads", description = "Lista todas as origens de leads com paginação")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<LeadOrigemResponseDto>>> findAll(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LeadOrigemResponseDto> result = leadOrigemService.findAll(pageable);
        PageResponseDto<LeadOrigemResponseDto> response = PageResponseDto.fromPage(result);

        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Origem por cliente", description = "Busca a origem de um cliente específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<LeadOrigemResponseDto>> findByCliente(
            @Parameter(description = "ID do cliente") @PathVariable Long clienteId) {

        return leadOrigemService.findByCliente(clienteId)
                .map(origem -> ResponseEntity.ok(ApiResponseDto.success(origem)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("Origem não encontrada para este cliente")));
    }

    @GetMapping("/utm-source/{utmSource}")
    @Operation(summary = "Leads por UTM source", description = "Lista leads de uma fonte UTM específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<LeadOrigemResponseDto>>> findByUtmSource(
            @Parameter(description = "UTM Source") @PathVariable String utmSource) {

        List<LeadOrigemResponseDto> origens = leadOrigemService.findByUtmSource(utmSource);
        return ResponseEntity.ok(ApiResponseDto.success(origens));
    }

    @GetMapping("/utm-sources")
    @Operation(summary = "Listar UTM sources", description = "Lista todas as fontes UTM distintas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<String>>> findDistinctUtmSources() {
        List<String> sources = leadOrigemService.findDistinctUtmSources();
        return ResponseEntity.ok(ApiResponseDto.success(sources));
    }

    @GetMapping("/analytics/utm")
    @Operation(summary = "Analytics UTM", description = "Retorna análise de performance das fontes UTM")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<UtmAnalyticsDto>>> getUtmAnalytics() {
        List<UtmAnalyticsDto> analytics = leadOrigemService.getUtmAnalytics();
        return ResponseEntity.ok(ApiResponseDto.success(analytics));
    }

    @GetMapping("/analytics/utm/{utmSource}/campaigns")
    @Operation(summary = "Analytics de campanhas", description = "Análise de performance das campanhas de uma fonte UTM")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<UtmAnalyticsDto>>> getUtmCampaignAnalytics(
            @Parameter(description = "UTM Source") @PathVariable String utmSource) {

        List<UtmAnalyticsDto> analytics = leadOrigemService.getUtmCampaignAnalytics(utmSource);
        return ResponseEntity.ok(ApiResponseDto.success(analytics));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar origem por ID", description = "Retorna os dados de uma origem específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<LeadOrigemResponseDto>> findById(
            @Parameter(description = "ID da origem") @PathVariable Long id) {

        return leadOrigemService.findById(id)
                .map(origem -> ResponseEntity.ok(ApiResponseDto.success(origem)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("Origem não encontrada")));
    }

    @PostMapping
    @Operation(summary = "Criar origem", description = "Registra origem de um lead")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<LeadOrigemResponseDto>> create(
            @Parameter(description = "Dados da origem") @RequestBody @Valid LeadOrigemRequestDto request) {

        try {
            LeadOrigemResponseDto origem = leadOrigemService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Origem registrada com sucesso", origem));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar origem", description = "Atualiza os dados de uma origem existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<LeadOrigemResponseDto>> update(
            @Parameter(description = "ID da origem") @PathVariable Long id,
            @Parameter(description = "Novos dados da origem") @RequestBody @Valid LeadOrigemUpdateDto request) {

        try {
            LeadOrigemResponseDto origem = leadOrigemService.update(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Origem atualizada com sucesso", origem));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir origem", description = "Remove uma origem do sistema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @Parameter(description = "ID da origem") @PathVariable Long id) {

        try {
            leadOrigemService.delete(id);
            return ResponseEntity.ok(ApiResponseDto.success("Origem excluída com sucesso", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }
}

