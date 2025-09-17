package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.InteracaoCliente;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nakacorp.backend.model.enums.TipoInteracao;

import java.time.LocalDateTime;
import java.util.Map;

public record TimelineClienteDto(
        Long interacaoId,
        TipoInteracao tipoInteracao,
        String descricao,
        String usuarioNome,
        Map<String, Object> dadosExtras,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime dataInteracao
) {
    public static TimelineClienteDto fromEntity(InteracaoCliente interacao) {
        return new TimelineClienteDto(
                interacao.getId(),
                interacao.getTipoInteracao(),
                interacao.getDescricao(),
                interacao.getUsuario() != null ? interacao.getUsuario().getNome() : "Sistema",
                interacao.getDadosExtras(),
                interacao.getCreatedAt()
        );
    }
}