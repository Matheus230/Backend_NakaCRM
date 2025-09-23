package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.dto.res.DashboardStatsDto;
import com.nakacorp.backend.dto.res.LeadConversionDto;
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

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Estatísticas e métricas do sistema CRM")
@SecurityRequirement(name = "bearer-jwt")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

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
}