package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.enums.TipoInteracao;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO para próximas tarefas e reuniões agendadas.
 * <p>
 * Representa interações futuras que precisam ser realizadas.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
public record ProximaInteracaoDto(
        Long id,
        String clienteNome,
        Long clienteId,
        TipoInteracao tipo,
        String descricao,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime dataAgendada,

        String responsavel,
        boolean urgente
) {}
