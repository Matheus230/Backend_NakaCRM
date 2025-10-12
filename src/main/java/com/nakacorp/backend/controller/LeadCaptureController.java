package com.nakacorp.backend.controller;

import com.nakacorp.backend.dto.req.LeadCaptureRequestDto;
import com.nakacorp.backend.dto.res.ApiResponseDto;
import com.nakacorp.backend.dto.res.LeadCaptureResponseDto;
import com.nakacorp.backend.service.LeadCaptureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller público para captação de leads via formulário.
 * <p>
 * Este controller fornece um endpoint público (sem autenticação) que permite
 * a captação de leads através de formulários em landing pages, eventos,
 * palestras, etc.
 * </p>
 * <p>
 * O endpoint registra o lead no sistema e envia um email de confirmação
 * automaticamente.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 2025-01-12
 */
@RestController
@RequestMapping("/public/leads")
@Tag(name = "Captação de Leads (Público)", description = "Endpoints públicos para captação de leads via formulário")
@CrossOrigin(origins = "*")
public class LeadCaptureController {

    private static final Logger logger = LoggerFactory.getLogger(LeadCaptureController.class);

    private final LeadCaptureService leadCaptureService;

    @Autowired
    public LeadCaptureController(LeadCaptureService leadCaptureService) {
        this.leadCaptureService = leadCaptureService;
    }

    /**
     * Endpoint público para captação de leads via formulário.
     * <p>
     * Este endpoint não requer autenticação e pode ser usado por qualquer
     * formulário público para registrar interesse de potenciais clientes.
     * </p>
     * <p>
     * Funcionalidades:
     * - Cria ou atualiza o cliente no banco de dados
     * - Envia email de confirmação automático
     * - Define status do lead como NOVO
     * - Registra origem como LANDING_PAGE
     * </p>
     *
     * @param request Dados do lead capturado
     * @return ResponseEntity contendo informações de confirmação
     */
    @PostMapping("/capture")
    @Operation(
            summary = "Capturar novo lead",
            description = "Endpoint público para captação de leads via formulário. " +
                    "Registra o interesse do lead e envia email de confirmação."
    )
    public ResponseEntity<ApiResponseDto<LeadCaptureResponseDto>> capturarLead(
            @Parameter(description = "Dados do lead a ser capturado")
            @RequestBody @Valid LeadCaptureRequestDto request) {

        try {
            LeadCaptureResponseDto response = leadCaptureService.capturarLead(request);

            logger.info("Lead capturado com sucesso - Cliente ID: {}", response.clienteId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success(
                            "Lead capturado com sucesso! Você receberá um email de confirmação em breve.",
                            response
                    ));

        } catch (IllegalArgumentException e) {
            logger.warn("Erro de validação ao capturar lead: {}", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error(e.getMessage()));

        } catch (Exception e) {
            logger.error("Erro inesperado ao capturar lead", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error(
                            "Erro ao processar sua solicitação. Por favor, tente novamente mais tarde."
                    ));
        }
    }

    /**
     * Endpoint de health check para verificar se o serviço de captação está disponível.
     *
     * @return Status do serviço
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Verifica se o serviço de captação de leads está disponível"
    )
    public ResponseEntity<ApiResponseDto<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponseDto.success(
                        "Serviço de captação de leads está operacional",
                        "OK"
                )
        );
    }
}
