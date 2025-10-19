package com.nakacorp.backend.service;

import com.nakacorp.backend.dto.res.*;
import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.ClienteInteresse;
import com.nakacorp.backend.model.InteracaoCliente;
import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;
import com.nakacorp.backend.repository.ClienteRepository;
import com.nakacorp.backend.repository.InteracaoClienteRepository;
import com.nakacorp.backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final InteracaoClienteService interacaoService;
    private final InteracaoClienteRepository interacaoClienteRepository;

    @Autowired
    public DashboardService(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            InteracaoClienteService interacaoService,
            InteracaoClienteRepository interacaoClienteRepository) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.interacaoService = interacaoService;
        this.interacaoClienteRepository = interacaoClienteRepository;
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
        List<StatusLead> statusAtivos = Arrays.asList(
            StatusLead.NOVO,
            StatusLead.CONTATADO,
            StatusLead.QUALIFICADO
        );

        // Query otimizada - não carrega todos os clientes na memória
        return clienteRepository.findLeadsToFollow(seteDiasAtras, statusAtivos);
    }

    public List<Cliente> getLeadsHotToday() {
        LocalDateTime inicioHoje = LocalDateTime.now().toLocalDate().atStartOfDay();
        List<StatusLead> statusQuentes = Arrays.asList(
            StatusLead.QUALIFICADO,
            StatusLead.OPORTUNIDADE
        );

        // Query otimizada - não carrega todos os clientes na memória
        return clienteRepository.findLeadsHotToday(inicioHoje, statusQuentes);
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

    /**
     * Calcula métricas de receita total do CRM.
     * Baseado nos produtos associados aos clientes convertidos.
     *
     * @return DTO com informações de receita
     */
    public ReceitaTotalDto getReceitaTotal() {
        List<Cliente> clientesConvertidos = clienteRepository.findClientesConvertidosComInteresses();

        BigDecimal receitaTotal = BigDecimal.ZERO;
        BigDecimal receitaMensal = BigDecimal.ZERO;
        BigDecimal receitaAnual = BigDecimal.ZERO;
        long totalVendas = 0;

        for (Cliente cliente : clientesConvertidos) {
            if (cliente.getInteresses() != null) {
                for (ClienteInteresse interesse : cliente.getInteresses()) {
                    if (interesse.getProduto() != null && interesse.getProduto().getPreco() != null) {
                        BigDecimal preco = interesse.getProduto().getPreco();
                        receitaTotal = receitaTotal.add(preco);
                        totalVendas++;

                        // Cálculo baseado no tipo de cobrança
                        if (interesse.getProduto().getTipoCobranca() != null) {
                            switch (interesse.getProduto().getTipoCobranca()) {
                                case MENSAL -> receitaMensal = receitaMensal.add(preco);
                                case ANUAL -> receitaAnual = receitaAnual.add(preco);
                                case UNICO -> {} // Receita única não entra em recorrência
                            }
                        }
                    }
                }
            }
        }

        // Calcula receita potencial (todos os leads ativos com interesses)
        List<Cliente> clientesAtivos = clienteRepository.findClientesComInteressesParaPipeline();
        BigDecimal receitaPotencial = clientesAtivos.stream()
                .flatMap(c -> c.getInteresses() != null ? c.getInteresses().stream() : Stream.empty())
                .map(i -> i.getProduto() != null ? i.getProduto().getPreco() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calcula vendas no mês atual
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        List<Cliente> clientesMes = clienteRepository.findByCreatedAtBetween(inicioMes, LocalDateTime.now());
        long vendasMes = clientesMes.stream()
                .filter(c -> c.getStatusLead() == StatusLead.CLIENTE)
                .count();

        // Ticket médio
        BigDecimal ticketMedio = totalVendas > 0
                ? receitaTotal.divide(BigDecimal.valueOf(totalVendas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new ReceitaTotalDto(
                receitaTotal,
                receitaMensal,
                receitaAnual,
                receitaPotencial,
                totalVendas,
                vendasMes,
                ticketMedio
        );
    }

    /**
     * Retorna visão geral do pipeline de vendas por status.
     *
     * @return Lista de status com quantidade e valor potencial
     */
    public List<PipelineStatusDto> getPipelineVendas() {
        List<Cliente> todosClientes = clienteRepository.findClientesComInteressesParaPipeline();
        long totalLeads = todosClientes.size();

        // Agrupa por status e calcula valores
        Map<StatusLead, List<Cliente>> clientesPorStatus = todosClientes.stream()
                .collect(Collectors.groupingBy(Cliente::getStatusLead));

        return Arrays.stream(StatusLead.values())
                .filter(status -> status != StatusLead.PERDIDO) // Não mostra perdidos no pipeline
                .map(status -> {
                    List<Cliente> clientes = clientesPorStatus.getOrDefault(status, Collections.emptyList());
                    long quantidade = clientes.size();

                    BigDecimal valorPotencial = clientes.stream()
                            .flatMap(c -> c.getInteresses() != null ? c.getInteresses().stream() : Stream.empty())
                            .map(i -> i.getProduto() != null ? i.getProduto().getPreco() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double percentual = totalLeads > 0 ? (double) quantidade / totalLeads * 100 : 0.0;

                    return new PipelineStatusDto(status, quantidade, valorPotencial, percentual);
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca próximas interações/tarefas agendadas.
     * Como o sistema armazena agendamentos em JSONB, esta implementação
     * retorna as interações mais recentes que podem ter follow-up.
     *
     * @return Lista de próximas interações
     */
    public List<ProximaInteracaoDto> getProximasInteracoes() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime seteDiasAtras = agora.minus(7, ChronoUnit.DAYS);

        List<InteracaoCliente> interacoes = interacaoClienteRepository.findProximasInteracoes(seteDiasAtras);

        return interacoes.stream()
                .limit(10) // Limita a 10 próximas interações
                .map(i -> {
                    // Verifica se tem data agendada no dadosExtras
                    LocalDateTime dataAgendada = extractDataAgendada(i);
                    boolean urgente = isUrgente(i, dataAgendada);

                    return new ProximaInteracaoDto(
                            i.getId(),
                            i.getCliente() != null ? i.getCliente().getNome() : "N/A",
                            i.getCliente() != null ? i.getCliente().getId() : null,
                            i.getTipoInteracao(),
                            i.getDescricao(),
                            dataAgendada != null ? dataAgendada : i.getCreatedAt(),
                            i.getUsuario() != null ? i.getUsuario().getNome() : "Não atribuído",
                            urgente
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Retorna dados consolidados para a tela home do dashboard.
     *
     * @return DTO com todas as informações do dashboard
     */
    public DashboardHomeDto getDashboardHome() {
        ReceitaTotalDto receita = getReceitaTotal();
        DashboardStatsDto estatisticas = getGeneralStats();
        List<PipelineStatusDto> pipeline = getPipelineVendas();
        List<ProximaInteracaoDto> proximasInteracoes = getProximasInteracoes();
        double taxaConversao = calculateConversionRate();
        long leadsAtivos = clienteRepository.countLeadsAtivos();

        return new DashboardHomeDto(
                receita,
                estatisticas,
                pipeline,
                proximasInteracoes,
                taxaConversao,
                leadsAtivos
        );
    }

    /**
     * Extrai data agendada do campo dadosExtras (JSONB).
     */
    private LocalDateTime extractDataAgendada(InteracaoCliente interacao) {
        if (interacao.getDadosExtras() != null && interacao.getDadosExtras().containsKey("dataAgendada")) {
            Object dataObj = interacao.getDadosExtras().get("dataAgendada");
            if (dataObj != null) {
                try {
                    return LocalDateTime.parse(dataObj.toString());
                } catch (Exception e) {
                    // Se não conseguir parsear, retorna null
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * Verifica se uma interação é urgente (próxima de acontecer).
     */
    private boolean isUrgente(InteracaoCliente interacao, LocalDateTime dataAgendada) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime dataReferencia = dataAgendada != null ? dataAgendada : interacao.getCreatedAt();

        // Considera urgente se for nas próximas 24h
        long horasAte = ChronoUnit.HOURS.between(agora, dataReferencia);
        return horasAte >= 0 && horasAte <= 24;
    }
}
