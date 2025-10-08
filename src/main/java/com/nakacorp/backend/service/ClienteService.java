package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.req.ClienteRequestDto;
import com.nakacorp.backend.dto.req.ClienteUpdateDto;
import com.nakacorp.backend.dto.res.ClienteResponseDto;
import com.nakacorp.backend.dto.res.ClienteSummaryDto;
import com.nakacorp.backend.dto.res.DashboardStatsDto;
import com.nakacorp.backend.dto.res.FilterRequestDto;
import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.enums.StatusLead;
import com.nakacorp.backend.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final InteracaoClienteService interacaoService;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository, InteracaoClienteService interacaoService) {
        this.clienteRepository = clienteRepository;
        this.interacaoService = interacaoService;
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDto> findAll(Pageable pageable) {
        return clienteRepository.findAll(pageable)
                .map(ClienteResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<ClienteResponseDto> findById(Long id) {
        return clienteRepository.findById(id)
                .map(ClienteResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDto> findWithFilters(FilterRequestDto filter) {
        Sort sort = Sort.by(Sort.Direction.fromString(filter.sortDirection()), filter.sortBy());
        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);

        //FILTROS CUSTOM?
        return clienteRepository.findAll(pageable)
                .map(ClienteResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ClienteSummaryDto> findByStatus(StatusLead status) {
        return clienteRepository.findByStatusLead(status)
                .stream()
                .map(ClienteSummaryDto::fromEntity)
                .toList();
    }

    public ClienteResponseDto create(ClienteRequestDto request) {
        if (clienteRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Cliente já cadastrado");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setEmail(request.email());
        cliente.setOrigemLead(request.origemLead());
        updateClienteFromRequest(cliente, request);

        Cliente saved = clienteRepository.save(cliente);

        interacaoService.registrarPrimeiroContato(saved.getId());

        return ClienteResponseDto.fromEntity(saved);
    }

    public ClienteResponseDto update(Long id, ClienteUpdateDto request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + id));

        if (request.email() != null && !request.email().equals(cliente.getEmail())) {
            if (clienteRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("user já cadastrado");
            }
        }

        updateClienteFromUpdateRequest(cliente, request);
        Cliente updated = clienteRepository.save(cliente);
        return ClienteResponseDto.fromEntity(updated);
    }

    public ClienteResponseDto updateStatus(Long id, StatusLead novoStatus) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + id));

        StatusLead statusAnterior = cliente.getStatusLead();
        cliente.setStatusLead(novoStatus);
        cliente.setDataUltimaInteracao(LocalDateTime.now());

        if (novoStatus == StatusLead.CONTATADO && cliente.getDataPrimeiroContato() == null) {
            cliente.setDataPrimeiroContato(LocalDateTime.now());
        }

        Cliente updated = clienteRepository.save(cliente);

        interacaoService.registrarMudancaStatus(id, statusAnterior, novoStatus);

        return ClienteResponseDto.fromEntity(updated);
    }

    public void delete(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new IllegalArgumentException("Cliente não encontrado: " + id);
        }
        clienteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getStats() {
        long total = clienteRepository.count();
        long novos = clienteRepository.countByStatusLead(StatusLead.NOVO);
        long qualificados = clienteRepository.countByStatusLead(StatusLead.QUALIFICADO);
        long convertidos = clienteRepository.countByStatusLead(StatusLead.CLIENTE);
        long perdidos = clienteRepository.countByStatusLead(StatusLead.PERDIDO);

        return new DashboardStatsDto(
                total, novos, qualificados, convertidos, perdidos,
                0L, 0L, null
        );
    }

    private void updateClienteFromRequest(Cliente cliente, ClienteRequestDto request) {
        cliente.setTelefone(request.telefone());
        cliente.setEndereco(request.endereco());
        cliente.setCidade(request.cidade());
        cliente.setEstado(request.estado());
        cliente.setCep(request.cep());
        cliente.setEmpresa(request.empresa());
        cliente.setCargo(request.cargo());
        cliente.setStatusLead(request.statusLead());
        cliente.setObservacoes(request.observacoes());
    }

    private void updateClienteFromUpdateRequest(Cliente cliente, ClienteUpdateDto request) {
        if (request.nome() != null) cliente.setNome(request.nome());
        if (request.email() != null) cliente.setEmail(request.email());
        if (request.telefone() != null) cliente.setTelefone(request.telefone());
        if (request.endereco() != null) cliente.setEndereco(request.endereco());
        if (request.cidade() != null) cliente.setCidade(request.cidade());
        if (request.estado() != null) cliente.setEstado(request.estado());
        if (request.cep() != null) cliente.setCep(request.cep());
        if (request.empresa() != null) cliente.setEmpresa(request.empresa());
        if (request.cargo() != null) cliente.setCargo(request.cargo());
        if (request.origemLead() != null) cliente.setOrigemLead(request.origemLead());
        if (request.statusLead() != null) cliente.setStatusLead(request.statusLead());
        if (request.dataPrimeiroContato() != null) cliente.setDataPrimeiroContato(request.dataPrimeiroContato());
        if (request.dataUltimaInteracao() != null) cliente.setDataUltimaInteracao(request.dataUltimaInteracao());
        if (request.observacoes() != null) cliente.setObservacoes(request.observacoes());
    }
}
