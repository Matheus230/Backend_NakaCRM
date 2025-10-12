package com.nakacorp.backend.dto.res;

import java.time.LocalDateTime;

/**
 * DTO de resposta para captação de lead.
 * <p>
 * Retorna informações sobre o lead capturado e confirma
 * o recebimento das informações.
 * </p>
 *
 * @param clienteId ID do cliente criado no sistema
 * @param nome Nome do lead
 * @param email Email do lead
 * @param mensagem Mensagem de confirmação
 * @param dataCaptura Data e hora da captação
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 2025-01-12
 */
public record LeadCaptureResponseDto(
        Long clienteId,
        String nome,
        String email,
        String mensagem,
        LocalDateTime dataCaptura
) {
    /**
     * Factory method para criar um DTO de resposta a partir dos dados do lead
     */
    public static LeadCaptureResponseDto from(
            Long clienteId,
            String nome,
            String email
    ) {
        return new LeadCaptureResponseDto(
                clienteId,
                nome,
                email,
                "Suas informações foram recebidas com sucesso! Em breve nossa equipe entrará em contato.",
                LocalDateTime.now()
        );
    }
}
