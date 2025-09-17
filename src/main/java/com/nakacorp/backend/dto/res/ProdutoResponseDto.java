package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.Produto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nakacorp.backend.model.enums.TipoCobranca;
import com.nakacorp.backend.model.enums.TipoPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponseDto(
        Long id,
        String nome,
        String descricao,
        String categoria,
        BigDecimal preco,
        Boolean pago,
        TipoPagamento tipoPagamento,
        TipoCobranca tipoCobranca,
        Boolean ativo,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
    public static ProdutoResponseDto fromEntity(Produto produto) {
        return new ProdutoResponseDto(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria(),
                produto.getPreco(),
                produto.getPago(),
                produto.getTipoPagamento(),
                produto.getTipoCobranca(),
                produto.getAtivo(),
                produto.getCreatedAt(),
                produto.getUpdatedAt()
        );
    }
}
