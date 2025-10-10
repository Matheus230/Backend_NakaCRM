package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.req.ClienteInteresseRequestDto;
import com.nakacorp.backend.dto.req.ClienteInteresseUpdateDto;
import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.dto.res.ClienteInteresseResponseDto;
import com.nakacorp.backend.dto.res.InteresseProdutoStatsDto;
import com.nakacorp.backend.dto.res.PageResponseDto;
import com.nakacorp.backend.model.enums.NivelInteresse;
import com.nakacorp.backend.service.ClienteInteresseService;
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
 * Controller REST para gerenciamento de Interesses dos Clientes em Produtos
 * <p>
 * Permite rastrear e gerenciar o interesse dos clientes em produtos/serviços específicos,
 * incluindo o nível de interesse (BAIXO, MEDIO, ALTO) e observações relevantes.
 * Útil para priorização de vendas e análise de produtos mais desejados.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/cliente-interesses")
@Tag(name = "Interesses", description = "Gestão de interesses dos clientes por produtos")
@SecurityRequirement(name = "bearer-jwt")
public class ClienteInteresseController {

    private final ClienteInteresseService interesseService;

    /**
     * Construtor com injeção de dependência do serviço de interesses.
     *
     * @param interesseService serviço de gerenciamento de interesses
     */
    @Autowired
    public ClienteInteresseController(ClienteInteresseService interesseService) {
        this.interesseService = interesseService;
    }

    @GetMapping
    @Operation(summary = "Listar interesses", description = "Lista todos os interesses com paginação")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ClienteInteresseResponseDto>>> findAll(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ClienteInteresseResponseDto> result = interesseService.findAll(pageable);
        PageResponseDto<ClienteInteresseResponseDto> response = PageResponseDto.fromPage(result);

        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Interesses por cliente", description = "Lista todos os interesses de um cliente específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<ClienteInteresseResponseDto>>> findByCliente(
            @Parameter(description = "ID do cliente") @PathVariable Long clienteId) {

        List<ClienteInteresseResponseDto> interesses = interesseService.findByCliente(clienteId);
        return ResponseEntity.ok(ApiResponseDto.success(interesses));
    }

    @GetMapping("/produto/{produtoId}")
    @Operation(summary = "Interesses por produto", description = "Lista todos os clientes interessados em um produto")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<ClienteInteresseResponseDto>>> findByProduto(
            @Parameter(description = "ID do produto") @PathVariable Long produtoId) {

        List<ClienteInteresseResponseDto> interesses = interesseService.findByProduto(produtoId);
        return ResponseEntity.ok(ApiResponseDto.success(interesses));
    }

    @GetMapping("/nivel/{nivel}")
    @Operation(summary = "Interesses por nível", description = "Lista interesses por nível específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<ClienteInteresseResponseDto>>> findByNivel(
            @Parameter(description = "Nível de interesse") @PathVariable NivelInteresse nivel) {

        List<ClienteInteresseResponseDto> interesses = interesseService.findByNivelInteresse(nivel);
        return ResponseEntity.ok(ApiResponseDto.success(interesses));
    }

    @GetMapping("/interesse-alto")
    @Operation(summary = "Clientes com interesse alto", description = "Lista clientes com interesse alto em produtos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<ClienteInteresseResponseDto>>> getClientesInteresseAlto() {
        List<ClienteInteresseResponseDto> interesses = interesseService.getClientesInteresseAlto();
        return ResponseEntity.ok(ApiResponseDto.success(interesses));
    }

    @GetMapping("/produtos-mais-desejados")
    @Operation(summary = "Produtos mais desejados", description = "Lista produtos ordenados por quantidade de interessados")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<InteresseProdutoStatsDto>>> getProdutosMaisDesejados() {
        List<InteresseProdutoStatsDto> stats = interesseService.getProdutosMaisDesejados();
        return ResponseEntity.ok(ApiResponseDto.success(stats));
    }

    @GetMapping("/cliente/{clienteId}/produto/{produtoId}")
    @Operation(summary = "Buscar interesse específico", description = "Busca interesse de um cliente em um produto específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ClienteInteresseResponseDto>> findByClienteAndProduto(
            @Parameter(description = "ID do cliente") @PathVariable Long clienteId,
            @Parameter(description = "ID do produto") @PathVariable Long produtoId) {

        return interesseService.findByClienteAndProduto(clienteId, produtoId)
                .map(interesse -> ResponseEntity.ok(ApiResponseDto.success(interesse)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("Interesse não encontrado")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar interesse por ID", description = "Retorna os dados de um interesse específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ClienteInteresseResponseDto>> findById(
            @Parameter(description = "ID do interesse") @PathVariable Long id) {

        return interesseService.findById(id)
                .map(interesse -> ResponseEntity.ok(ApiResponseDto.success(interesse)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("Interesse não encontrado")));
    }

    @PostMapping
    @Operation(summary = "Criar interesse", description = "Registra interesse de um cliente em um produto")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ClienteInteresseResponseDto>> create(
            @Parameter(description = "Dados do interesse") @RequestBody @Valid ClienteInteresseRequestDto request) {

        try {
            ClienteInteresseResponseDto interesse = interesseService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Interesse registrado com sucesso", interesse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar interesse", description = "Atualiza os dados de um interesse existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ClienteInteresseResponseDto>> update(
            @Parameter(description = "ID do interesse") @PathVariable Long id,
            @Parameter(description = "Novos dados do interesse") @RequestBody @Valid ClienteInteresseUpdateDto request) {

        try {
            ClienteInteresseResponseDto interesse = interesseService.update(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Interesse atualizado com sucesso", interesse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir interesse", description = "Remove um interesse do sistema")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @Parameter(description = "ID do interesse") @PathVariable Long id) {

        try {
            interesseService.delete(id);
            return ResponseEntity.ok(ApiResponseDto.success("Interesse excluído com sucesso", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }
}