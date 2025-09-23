package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.req.ClienteInteresseRequestDto;
import com.nakacorp.backend.dto.req.ClienteInteresseUpdateDto;
import com.nakacorp.backend.dto.res.ClienteInteresseResponseDto;
import com.nakacorp.backend.dto.res.InteresseProdutoStatsDto;
import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.ClienteInteresse;
import com.nakacorp.backend.model.Produto;
import com.nakacorp.backend.model.enums.NivelInteresse;
import com.nakacorp.backend.repository.ClienteInteresseRepository;
import com.nakacorp.backend.repository.ClienteRepository;
import com.nakacorp.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteInteresseService {

    private final ClienteInteresseRepository interesseRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final InteracaoClienteService interacaoService;

    @Autowired
    public ClienteInteresseService(
            ClienteInteresseRepository interesseRepository,
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            InteracaoClienteService interacaoService) {
        this.interesseRepository = interesseRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.interacaoService = interacaoService;
    }

    @Transactional(readOnly = true)
    public Page<ClienteInteresseResponseDto> findAll(Pageable pageable) {
        return interesseRepository.findAll(pageable)
                .map(ClienteInteresseResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<ClienteInteresseResponseDto> findById(Long id) {
        return interesseRepository.findById(id)
                .map(ClienteInteresseResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ClienteInteresseResponseDto> findByCliente(Long clienteId) {
        return interesseRepository.findByClienteId(clienteId)
                .stream()
                .map(ClienteInteresseResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteInteresseResponseDto> findByProduto(Long produtoId) {
        return interesseRepository.findByProdutoId(produtoId)
                .stream()
                .map(ClienteInteresseResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteInteresseResponseDto> findByNivelInteresse(NivelInteresse nivel) {
        return interesseRepository.findByNivelInteresse(nivel)
                .stream()
                .map(ClienteInteresseResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ClienteInteresseResponseDto> findByClienteAndProduto(Long clienteId, Long produtoId) {
        return interesseRepository.findByClienteIdAndProdutoId(clienteId, produtoId)
                .map(ClienteInteresseResponseDto::fromEntity);
    }

    public ClienteInteresseResponseDto create(ClienteInteresseRequestDto request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + request.clienteId()));

        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + request.produtoId()));

        if (interesseRepository.findByClienteIdAndProdutoId(request.clienteId(), request.produtoId()).isPresent()) {
            throw new IllegalArgumentException("Cliente já possui interesse cadastrado neste produto");
        }

        ClienteInteresse interesse = new ClienteInteresse(cliente, produto);
        interesse.setNivelInteresse(request.nivelInteresse());
        interesse.setObservacoes(request.observacoes());

        ClienteInteresse saved = interesseRepository.save(interesse);

        interacaoService.registrarInteresseProduto(
                request.clienteId(),
                produto.getNome(),
                request.nivelInteresse()
        );

        return ClienteInteresseResponseDto.fromEntity(saved);
    }

    public ClienteInteresseResponseDto update(Long id, ClienteInteresseUpdateDto request) {
        ClienteInteresse interesse = interesseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interesse não encontrado: " + id));

        NivelInteresse nivelAnterior = interesse.getNivelInteresse();

        if (request.nivelInteresse() != null) interesse.setNivelInteresse(request.nivelInteresse());
        if (request.observacoes() != null) interesse.setObservacoes(request.observacoes());

        ClienteInteresse updated = interesseRepository.save(interesse);

        if (request.nivelInteresse() != null && !request.nivelInteresse().equals(nivelAnterior)) {
            interacaoService.registrarMudancaNivelInteresse(
                    interesse.getCliente().getId(),
                    interesse.getProduto().getNome(),
                    nivelAnterior,
                    request.nivelInteresse()
            );
        }

        return ClienteInteresseResponseDto.fromEntity(updated);
    }

    public void delete(Long id) {
        ClienteInteresse interesse = interesseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interesse não encontrado: " + id));

        interacaoService.registrarRemocaoInteresse(
                interesse.getCliente().getId(),
                interesse.getProduto().getNome()
        );

        interesseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<InteresseProdutoStatsDto> getProdutosMaisDesejados() {
        return produtoRepository.findByAtivoTrue()
                .stream()
                .map(produto -> {
                    long total = interesseRepository.countByProdutoId(produto.getId());
                    long alto = interesseRepository.countByProdutoIdAndNivelInteresse(
                            produto.getId(), NivelInteresse.ALTO);
                    long medio = interesseRepository.countByProdutoIdAndNivelInteresse(
                            produto.getId(), NivelInteresse.MEDIO);
                    long baixo = interesseRepository.countByProdutoIdAndNivelInteresse(
                            produto.getId(), NivelInteresse.BAIXO);

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
                })
                .filter(stats -> stats.totalInteressados() > 0)
                .sorted((a, b) -> Long.compare(b.totalInteressados(), a.totalInteressados()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClienteInteresseResponseDto> getClientesInteresseAlto() {
        return interesseRepository.findByNivelInteresse(NivelInteresse.ALTO)
                .stream()
                .map(ClienteInteresseResponseDto::fromEntity)
                .toList();
    }
}