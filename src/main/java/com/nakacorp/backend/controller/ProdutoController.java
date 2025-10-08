package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.req.ProdutoRequestDto;
import com.nakacorp.backend.dto.req.ProdutoUpdateDto;
import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.dto.res.InteresseProdutoStatsDto;
import com.nakacorp.backend.dto.res.PageResponseDto;
import com.nakacorp.backend.dto.res.ProdutoResponseDto;
import com.nakacorp.backend.dto.res.ProdutoSummaryDto;
import com.nakacorp.backend.service.ProdutoService;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller REST para gerenciamento de Produtos e Serviços
 * <p>
 * Gerencia o catálogo de produtos e serviços oferecidos pela empresa.
 * Suporta categorização, precificação, ativação/desativação de produtos,
 * filtros por categoria e faixa de preço, além de estatísticas de interesse
 * dos clientes por produto. Essencial para gestão de vendas e CRM.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/produtos")
@Tag(name = "Produtos", description = "Gestão de produtos e serviços")
@SecurityRequirement(name = "bearer-jwt")
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final ProdutoService produtoService;

    /**
     * Construtor com injeção de dependência do serviço de produtos.
     *
     * @param produtoService serviço de gerenciamento de produtos e serviços
     */
    @Autowired
    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Lista todos os produtos com paginação")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ProdutoResponseDto>>> findAll(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "nome") String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "asc") String sortDirection,
            @Parameter(description = "Filtrar apenas ativos") @RequestParam(defaultValue = "false") boolean apenasAtivos) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProdutoResponseDto> result = apenasAtivos ?
                produtoService.findAtivos(pageable) :
                produtoService.findAll(pageable);

        PageResponseDto<ProdutoResponseDto> response = PageResponseDto.fromPage(result);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorias", description = "Lista todas as categorias de produtos ativas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<String>>> findCategorias() {
        List<String> categorias = produtoService.findCategorias();
        return ResponseEntity.ok(ApiResponseDto.success(categorias));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Buscar por categoria", description = "Lista produtos de uma categoria específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<ProdutoSummaryDto>>> findByCategoria(
            @Parameter(description = "Nome da categoria") @PathVariable String categoria) {

        List<ProdutoSummaryDto> produtos = produtoService.findByCategoria(categoria);
        return ResponseEntity.ok(ApiResponseDto.success(produtos));
    }

    @GetMapping("/preco")
    @Operation(summary = "Buscar por faixa de preço", description = "Lista produtos dentro de uma faixa de preço")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<ProdutoResponseDto>>> findByPrecoRange(
            @Parameter(description = "Preço mínimo") @RequestParam BigDecimal precoMin,
            @Parameter(description = "Preço máximo") @RequestParam BigDecimal precoMax) {

        List<ProdutoResponseDto> produtos = produtoService.findByPrecoRange(precoMin, precoMax);
        return ResponseEntity.ok(ApiResponseDto.success(produtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna os dados de um produto específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ProdutoResponseDto>> findById(
            @Parameter(description = "ID do produto") @PathVariable Long id) {

        return produtoService.findById(id)
                .map(produto -> ResponseEntity.ok(ApiResponseDto.success(produto)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error("Produto não encontrado")));
    }

    @GetMapping("/{id}/interesse-stats")
    @Operation(summary = "Estatísticas de interesse", description = "Retorna estatísticas de interesse em um produto")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<InteresseProdutoStatsDto>> getInteresseStats(
            @Parameter(description = "ID do produto") @PathVariable Long id) {

        try {
            InteresseProdutoStatsDto stats = produtoService.getInteresseStats(id);
            return ResponseEntity.ok(ApiResponseDto.success(stats));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Criar produto", description = "Cria um novo produto no sistema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<ProdutoResponseDto>> create(
            @Parameter(description = "Dados do produto") @RequestBody @Valid ProdutoRequestDto request) {

        ProdutoResponseDto produto = produtoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Produto criado com sucesso", produto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<ProdutoResponseDto>> update(
            @Parameter(description = "ID do produto") @PathVariable Long id,
            @Parameter(description = "Novos dados do produto") @RequestBody @Valid ProdutoUpdateDto request) {

        try {
            ProdutoResponseDto produto = produtoService.update(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Produto atualizado com sucesso", produto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-ativo")
    @Operation(summary = "Alternar status ativo", description = "Ativa ou desativa um produto")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<ProdutoResponseDto>> toggleAtivo(
            @Parameter(description = "ID do produto") @PathVariable Long id) {

        try {
            ProdutoResponseDto produto = produtoService.toggleAtivo(id);
            return ResponseEntity.ok(ApiResponseDto.success("Status do produto alterado com sucesso", produto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir produto", description = "Remove um produto do sistema")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @Parameter(description = "ID do produto") @PathVariable Long id) {

        try {
            produtoService.delete(id);
            return ResponseEntity.ok(ApiResponseDto.success("Produto excluído com sucesso", null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));
        }
    }
}