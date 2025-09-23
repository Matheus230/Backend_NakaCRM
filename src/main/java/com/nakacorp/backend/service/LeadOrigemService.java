package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.req.LeadOrigemRequestDto;
import com.nakacorp.backend.dto.req.LeadOrigemUpdateDto;
import com.nakacorp.backend.dto.res.LeadOrigemResponseDto;
import com.nakacorp.backend.dto.res.UtmAnalyticsDto;
import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.LeadOrigem;
import com.nakacorp.backend.model.enums.StatusLead;
import com.nakacorp.backend.repository.ClienteRepository;
import com.nakacorp.backend.repository.LeadOrigemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LeadOrigemService {

    private final LeadOrigemRepository leadOrigemRepository;
    private final ClienteRepository clienteRepository;

    @Autowired
    public LeadOrigemService(LeadOrigemRepository leadOrigemRepository, ClienteRepository clienteRepository) {
        this.leadOrigemRepository = leadOrigemRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public Page<LeadOrigemResponseDto> findAll(Pageable pageable) {
        return leadOrigemRepository.findAll(pageable)
                .map(LeadOrigemResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<LeadOrigemResponseDto> findById(Long id) {
        return leadOrigemRepository.findById(id)
                .map(LeadOrigemResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<LeadOrigemResponseDto> findByCliente(Long clienteId) {
        return leadOrigemRepository.findByClienteId(clienteId)
                .map(LeadOrigemResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<LeadOrigemResponseDto> findByUtmSource(String utmSource) {
        return leadOrigemRepository.findByUtmSource(utmSource)
                .stream()
                .map(LeadOrigemResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> findDistinctUtmSources() {
        return leadOrigemRepository.findDistinctUtmSources();
    }

    public LeadOrigemResponseDto create(LeadOrigemRequestDto request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + request.clienteId()));

        // Verificar se já existe origem para este cliente
        if (leadOrigemRepository.findByClienteId(request.clienteId()).isPresent()) {
            throw new IllegalArgumentException("Cliente já possui origem cadastrada");
        }

        LeadOrigem leadOrigem = new LeadOrigem(cliente);
        updateLeadOrigemFromRequest(leadOrigem, request);

        LeadOrigem saved = leadOrigemRepository.save(leadOrigem);
        return LeadOrigemResponseDto.fromEntity(saved);
    }

    public LeadOrigemResponseDto update(Long id, LeadOrigemUpdateDto request) {
        LeadOrigem leadOrigem = leadOrigemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Origem do lead não encontrada: " + id));

        updateLeadOrigemFromUpdateRequest(leadOrigem, request);
        LeadOrigem updated = leadOrigemRepository.save(leadOrigem);
        return LeadOrigemResponseDto.fromEntity(updated);
    }

    public void delete(Long id) {
        if (!leadOrigemRepository.existsById(id)) {
            throw new IllegalArgumentException("Origem do lead não encontrada: " + id);
        }
        leadOrigemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UtmAnalyticsDto> getUtmAnalytics() {
        return leadOrigemRepository.findDistinctUtmSources()
                .stream()
                .map(this::calculateUtmStats)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtmAnalyticsDto> getUtmCampaignAnalytics(String utmSource) {
        return leadOrigemRepository.findByUtmSource(utmSource)
                .stream()
                .map(LeadOrigem::getUtmCampaign)
                .distinct()
                .filter(campaign -> campaign != null && !campaign.isEmpty())
                .map(campaign -> calculateCampaignStats(utmSource, campaign))
                .toList();
    }

    private void updateLeadOrigemFromRequest(LeadOrigem leadOrigem, LeadOrigemRequestDto request) {
        leadOrigem.setFonteDetalhada(request.fonteDetalhada());
        leadOrigem.setUtmSource(request.utmSource());
        leadOrigem.setUtmMedium(request.utmMedium());
        leadOrigem.setUtmCampaign(request.utmCampaign());
        leadOrigem.setUserAgent(request.userAgent());
    }

    private void updateLeadOrigemFromUpdateRequest(LeadOrigem leadOrigem, LeadOrigemUpdateDto request) {
        if (request.fonteDetalhada() != null) leadOrigem.setFonteDetalhada(request.fonteDetalhada());
        if (request.utmSource() != null) leadOrigem.setUtmSource(request.utmSource());
        if (request.utmMedium() != null) leadOrigem.setUtmMedium(request.utmMedium());
        if (request.utmCampaign() != null) leadOrigem.setUtmCampaign(request.utmCampaign());
        if (request.userAgent() != null) leadOrigem.setUserAgent(request.userAgent());
    }

    private UtmAnalyticsDto calculateUtmStats(String utmSource) {
        List<LeadOrigem> origens = leadOrigemRepository.findByUtmSource(utmSource);
        long totalLeads = origens.size();
        long convertidos = origens.stream()
                .filter(origem -> origem.getCliente().getStatusLead() == StatusLead.CLIENTE)
                .count();

        return new UtmAnalyticsDto(utmSource, null, totalLeads, convertidos);
    }

    private UtmAnalyticsDto calculateCampaignStats(String utmSource, String utmCampaign) {
        List<LeadOrigem> origens = leadOrigemRepository.findByUtmSourceAndUtmCampaign(utmSource, utmCampaign);
        long totalLeads = origens.size();
        long convertidos = origens.stream()
                .filter(origem -> origem.getCliente().getStatusLead() == StatusLead.CLIENTE)
                .count();

        return new UtmAnalyticsDto(utmSource, utmCampaign, totalLeads, convertidos);
    }
}