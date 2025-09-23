package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.res.DashboardStatsDto;
import com.nakacorp.backend.dto.res.InteracaoStatsDto;
import com.nakacorp.backend.dto.res.LeadConversionDto;
import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;
import com.nakacorp.backend.repository.ClienteRepository;
import com.nakacorp.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final InteracaoClienteService interacaoService;

    @Autowired
    public DashboardService(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            InteracaoClienteService interacaoService) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.interacaoService = interacaoService;
    }

    public DashboardStatsDto getGeneralStats() {
        // Stats de clientes
        long totalClientes = clienteRepository.count();
        long leadsNovos = clienteRepository.countByStatusLead(StatusLead.NOVO);
        long leadsQualificados = clienteRepository.countByStatusLead(StatusLead.QUALIFICADO);
        long leadsConvertidos = clienteRepository.countByStatusLead(StatusLead.CLIENTE);
        long leadsPerdidos = clienteRepository.countByStatusLead(StatusLead.PERDIDO);

        // Stats de produtos
        long totalProdutos = produtoRepository.count();
        long produtosAtivos = produtoRepository.findByAtivoTrue().size();

        // Stats de interações
        InteracaoStatsDto interacaoStats = interacaoService.getStats();

        return new DashboardStatsDto(
                totalClientes,
                leadsNovos,
                leadsQualificados,
                leadsConvertidos,
                leadsPerdidos,
                totalProdutos,
                produtosAtivos,
                interacaoStats
        );
    }

    public List<LeadConversionDto> getConversionRateByOrigem() {
        return Arrays.stream(OrigemLead.values())
                .map(this::calculateConversionForOrigem)
                .toList();
    }

    public DashboardStatsDto getStatsByPeriod(LocalDateTime inicio, LocalDateTime fim) {
        List<Cliente> clientesPeriodo = clienteRepository.findByCreatedAtBetween(inicio, fim);

        long totalClientes = clientesPeriodo.size();
        long leadsNovos = clientesPeriodo.stream()
                .filter(c -> c.getStatusLead() == StatusLead.NOVO)
                .count();
        long leadsQualificados = clientesPeriodo.stream()
                .filter(c -> c.getStatusLead() == StatusLead.QUALIFICADO)
                .count();
        long leadsConvertidos = clientesPeriodo.stream()
                .filter(c -> c.getStatusLead() == StatusLead.CLIENTE)
                .count();
        long leadsPerdidos = clientesPeriodo.stream()
                .filter(c -> c.getStatusLead() == StatusLead.PERDIDO)
                .count();

        long totalProdutos = produtoRepository.count();
        long produtosAtivos = produtoRepository.findByAtivoTrue().size();

        // Interações do período seriam necessárias no repository
        InteracaoStatsDto interacaoStats = interacaoService.getStats();

        return new DashboardStatsDto(
                totalClientes,
                leadsNovos,
                leadsQualificados,
                leadsConvertidos,
                leadsPerdidos,
                totalProdutos,
                produtosAtivos,
                interacaoStats
        );
    }

    public double calculateConversionRate() {
        long totalLeads = clienteRepository.count();
        long convertidos = clienteRepository.countByStatusLead(StatusLead.CLIENTE);

        return totalLeads > 0 ? (double) convertidos / totalLeads * 100 : 0.0;
    }

    public double calculateConversionRateLastMonth() {
        LocalDateTime umMesAtras = LocalDateTime.now().minus(1, ChronoUnit.MONTHS);
        List<Cliente> clientesUltimoMes = clienteRepository.findByCreatedAtBetween(umMesAtras, LocalDateTime.now());

        long totalLeads = clientesUltimoMes.size();
        long convertidos = clientesUltimoMes.stream()
                .filter(c -> c.getStatusLead() == StatusLead.CLIENTE)
                .count();

        return totalLeads > 0 ? (double) convertidos / totalLeads * 100 : 0.0;
    }

    public List<Cliente> getLeadsToFollow() {
        LocalDateTime seteDiasAtras = LocalDateTime.now().minus(7, ChronoUnit.DAYS);

        return clienteRepository.findAll().stream()
                .filter(cliente -> cliente.getDataUltimaInteracao() == null ||
                        cliente.getDataUltimaInteracao().isBefore(seteDiasAtras))
                .filter(cliente -> cliente.getStatusLead() == StatusLead.NOVO ||
                        cliente.getStatusLead() == StatusLead.CONTATADO ||
                        cliente.getStatusLead() == StatusLead.QUALIFICADO)
                .toList();
    }

    public List<Cliente> getLeadsHotToday() {
        LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();

        return clienteRepository.findAll().stream()
                .filter(cliente -> cliente.getStatusLead() == StatusLead.QUALIFICADO ||
                        cliente.getStatusLead() == StatusLead.OPORTUNIDADE)
                .filter(cliente -> cliente.getDataUltimaInteracao() != null &&
                        cliente.getDataUltimaInteracao().isAfter(inicioHoje))
                .toList();
    }

    private LeadConversionDto calculateConversionForOrigem(OrigemLead origem) {
        List<Cliente> clientesOrigem = clienteRepository.findByOrigemLead(origem);

        long totalLeads = clientesOrigem.size();
        long convertidos = clientesOrigem.stream()
                .filter(c -> c.getStatusLead() == StatusLead.CLIENTE)
                .count();

        double taxa = totalLeads > 0 ? (double) convertidos / totalLeads * 100 : 0.0;

        return new LeadConversionDto(origem, totalLeads, convertidos, taxa);
    }
}
