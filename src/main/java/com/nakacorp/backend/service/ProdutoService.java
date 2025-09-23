package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.req.ProdutoRequestDto;
import com.nakacorp.backend.dto.req.ProdutoUpdateDto;
import com.nakacorp.backend.dto.res.InteresseProdutoStatsDto;
import com.nakacorp.backend.dto.res.ProdutoResponseDto;
import com.nakacorp.backend.dto.res.ProdutoSummaryDto;
import com.nakacorp.backend.model.Produto;
import com.nakacorp.backend.model.enums.NivelInteresse;
import com.nakacorp.backend.repository.ClienteInteresseRepository;
import com.nakacorp.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ClienteInteresseRepository clienteInteresseRepository;

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository, ClienteInteresseRepository clienteInteresseRepository) {
        this.produtoRepository = produtoRepository;
        this.clienteInteresseRepository = clienteInteresseRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDto> findAll(Pageable pageable) {
        return produtoRepository.findAll(pageable)
                .map(ProdutoResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDto> findAtivos(Pageable pageable) {
        return produtoRepository.findAll(pageable)
                .map(ProdutoResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<ProdutoResponseDto> findById(Long id) {
        return produtoRepository.findById(id)
                .map(ProdutoResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ProdutoSummaryDto> findByCategoria(String categoria) {
        return produtoRepository.findByAtivoTrueAndCategoria(categoria)
                .stream()
                .map(ProdutoSummaryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDto> findByPrecoRange(BigDecimal precoMin, BigDecimal precoMax) {
        return produtoRepository.findByPrecoBetween(precoMin, precoMax)
                .stream()
                .map(ProdutoResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> findCategorias() {
        return produtoRepository.findDistinctCategorias();
    }

    public ProdutoResponseDto create(ProdutoRequestDto request) {
        Produto produto = new Produto(request.nome(), request.preco());
        updateProdutoFromRequest(produto, request);

        Produto saved = produtoRepository.save(produto);
        return ProdutoResponseDto.fromEntity(saved);
    }

    public ProdutoResponseDto update(Long id, ProdutoUpdateDto request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        updateProdutoFromUpdateRequest(produto, request);
        Produto updated = produtoRepository.save(produto);
        return ProdutoResponseDto.fromEntity(updated);
    }

    public ProdutoResponseDto toggleAtivo(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        produto.setAtivo(!produto.getAtivo());
        Produto updated = produtoRepository.save(produto);
        return ProdutoResponseDto.fromEntity(updated);
    }

    public void delete(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new IllegalArgumentException("Produto não encontrado: " + id);
        }

        long interessesCount = clienteInteresseRepository.countByProdutoId(id);
        if (interessesCount > 0) {
            throw new IllegalStateException("Não é possível excluir produto com interesses cadastrados");
        }

        produtoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public InteresseProdutoStatsDto getInteresseStats(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + produtoId));

        long total = clienteInteresseRepository.countByProdutoId(produtoId);
        long alto = clienteInteresseRepository.countByProdutoIdAndNivelInteresse(produtoId,
                NivelInteresse.ALTO);
        long medio = clienteInteresseRepository.countByProdutoIdAndNivelInteresse(produtoId,
                NivelInteresse.MEDIO);
        long baixo = clienteInteresseRepository.countByProdutoIdAndNivelInteresse(produtoId,
                NivelInteresse.BAIXO);

        return new InteresseProdutoStatsDto(
                produto.getId(),
                produto.getNome(),
                produto.getCategoria(),
                produto.getPreco(),
                total,
                alto,
                medio,
                baixo
        );
    }

    @Transactional(readOnly = true)
    public long countAtivos() {
        return produtoRepository.findByAtivoTrue().size();
    }

    @Transactional(readOnly = true)
    public long countTotal() {
        return produtoRepository.count();
    }

    private void updateProdutoFromRequest(Produto produto, ProdutoRequestDto request) {
        produto.setDescricao(request.descricao());
        produto.setCategoria(request.categoria());
        produto.setPago(request.pago());
        produto.setTipoPagamento(request.tipoPagamento());
        produto.setTipoCobranca(request.tipoCobranca());
        produto.setAtivo(request.ativo());
    }

    private void updateProdutoFromUpdateRequest(Produto produto, ProdutoUpdateDto request) {
        if (request.nome() != null) produto.setNome(request.nome());
        if (request.descricao() != null) produto.setDescricao(request.descricao());
        if (request.categoria() != null) produto.setCategoria(request.categoria());
        if (request.preco() != null) produto.setPreco(request.preco());
        if (request.pago() != null) produto.setPago(request.pago());
        if (request.tipoPagamento() != null) produto.setTipoPagamento(request.tipoPagamento());
        if (request.tipoCobranca() != null) produto.setTipoCobranca(request.tipoCobranca());
        if (request.ativo() != null) produto.setAtivo(request.ativo());
    }
}