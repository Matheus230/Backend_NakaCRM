package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.res.*;
import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST para Dashboard e Métricas do CRM
 * <p>
 * Fornece endpoints para visualização de estatísticas gerais do sistema,
 * incluindo taxas de conversão, leads por status, análise de origem,
 * e identificação de leads prioritários (quentes e para follow-up).
 * Essencial para tomada de decisões e acompanhamento de performance.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Estatísticas e métricas do sistema CRM")
@SecurityRequirement(name = "bearer-jwt")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Construtor com injeção de dependência do serviço de dashboard.
     *
     * @param dashboardService serviço de estatísticas e métricas
     */
    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas gerais", description = "Obtém estatísticas gerais do CRM")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<DashboardStatsDto>> getStats() {
        DashboardStatsDto stats = dashboardService.getGeneralStats();
        return ResponseEntity.ok(ApiResponseDto.success(stats));
    }

    @GetMapping("/stats/periodo")
    @Operation(summary = "Estatísticas por período", description = "Estatísticas filtradas por período específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<DashboardStatsDto>> getStatsByPeriod(
            @Parameter(description = "Data de início")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data de fim")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        DashboardStatsDto stats = dashboardService.getStatsByPeriod(inicio, fim);
        return ResponseEntity.ok(ApiResponseDto.success(stats));
    }

    @GetMapping("/conversion-rate")
    @Operation(summary = "Taxa de conversão geral", description = "Calcula a taxa de conversão geral de leads")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Double>> getConversionRate() {
        double rate = dashboardService.calculateConversionRate();
        return ResponseEntity.ok(ApiResponseDto.success(rate));
    }

    @GetMapping("/conversion-rate/last-month")
    @Operation(summary = "Taxa de conversão último mês", description = "Taxa de conversão dos leads do último mês")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Double>> getConversionRateLastMonth() {
        double rate = dashboardService.calculateConversionRateLastMonth();
        return ResponseEntity.ok(ApiResponseDto.success(rate));
    }

    @GetMapping("/conversion/by-origem")
    @Operation(summary = "Conversão por origem", description = "Taxa de conversão segmentada por origem do lead")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<LeadConversionDto>>> getConversionByOrigem() {
        List<LeadConversionDto> conversion = dashboardService.getConversionRateByOrigem();
        return ResponseEntity.ok(ApiResponseDto.success(conversion));
    }

    @GetMapping("/leads/follow-up")
    @Operation(summary = "Leads para follow-up", description = "Lista leads que precisam de acompanhamento")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<Cliente>>> getLeadsToFollow() {
        List<Cliente> leads = dashboardService.getLeadsToFollow();
        return ResponseEntity.ok(ApiResponseDto.success(leads));
    }

    @GetMapping("/leads/hot-today")
    @Operation(summary = "Leads quentes hoje", description = "Leads qualificados com atividade recente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<Cliente>>> getLeadsHotToday() {
        List<Cliente> leads = dashboardService.getLeadsHotToday();
        return ResponseEntity.ok(ApiResponseDto.success(leads));
    }

    /**
     * Endpoint principal para a tela home do dashboard.
     * Retorna todos os dados consolidados necessários para a página inicial.
     *
     * @return ResponseEntity contendo todas as métricas do dashboard
     */
    @GetMapping("/home")
    @Operation(
            summary = "Dashboard Home Completo",
            description = "Retorna todas as informações consolidadas para a tela home: " +
                    "receita total, estatísticas gerais, pipeline de vendas, próximas interações, " +
                    "taxa de conversão e leads ativos"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<DashboardHomeDto>> getDashboardHome() {
        DashboardHomeDto dashboardHome = dashboardService.getDashboardHome();
        return ResponseEntity.ok(ApiResponseDto.success("Dashboard carregado com sucesso", dashboardHome));
    }

    /**
     * Retorna métricas detalhadas de receita do CRM.
     *
     * @return ResponseEntity contendo informações de receita total, mensal, anual e potencial
     */
    @GetMapping("/receita")
    @Operation(
            summary = "Métricas de Receita",
            description = "Retorna receita total, receita mensal/anual recorrente, " +
                    "receita potencial, total de vendas e ticket médio"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<ReceitaTotalDto>> getReceitaTotal() {
        ReceitaTotalDto receita = dashboardService.getReceitaTotal();
        return ResponseEntity.ok(ApiResponseDto.success(receita));
    }

    /**
     * Retorna visão geral do pipeline de vendas.
     * Mostra quantidade de leads e valor potencial em cada estágio do funil.
     *
     * @return ResponseEntity contendo lista de status com métricas
     */
    @GetMapping("/pipeline")
    @Operation(
            summary = "Pipeline de Vendas",
            description = "Retorna visão geral do funil de vendas com quantidade de leads, " +
                    "valor potencial e percentual em cada estágio"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<PipelineStatusDto>>> getPipelineVendas() {
        List<PipelineStatusDto> pipeline = dashboardService.getPipelineVendas();
        return ResponseEntity.ok(ApiResponseDto.success(pipeline));
    }

    /**
     * Retorna próximas tarefas e reuniões agendadas.
     *
     * @return ResponseEntity contendo lista de interações futuras
     */
    @GetMapping("/proximas-interacoes")
    @Operation(
            summary = "Próximas Tarefas e Reuniões",
            description = "Retorna lista de próximas interações agendadas, incluindo " +
                    "reuniões, ligações e follow-ups prioritários"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<List<ProximaInteracaoDto>>> getProximasInteracoes() {
        List<ProximaInteracaoDto> proximasInteracoes = dashboardService.getProximasInteracoes();
        return ResponseEntity.ok(ApiResponseDto.success(proximasInteracoes));
    }

    /**
     * Retorna contagem de leads ativos no sistema.
     * Leads ativos são aqueles que não estão nos status CLIENTE ou PERDIDO.
     *
     * @return ResponseEntity contendo número de leads ativos
     */
    @GetMapping("/leads-ativos")
    @Operation(
            summary = "Total de Leads Ativos",
            description = "Retorna a contagem de leads que ainda estão no funil de vendas " +
                    "(excluindo clientes convertidos e leads perdidos)"
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    public ResponseEntity<ApiResponseDto<Long>> getLeadsAtivos() {
        long leadsAtivos = dashboardService.getDashboardHome().leadsAtivos();
        return ResponseEntity.ok(ApiResponseDto.success(leadsAtivos));
    }
}