package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.Produto;
import com.nakacorp.backend.model.enums.TipoCobranca;

import java.math.BigDecimal;

public record ProdutoSummaryDto(
        Long id,
        String nome,
        String categoria,
        BigDecimal preco,
        TipoCobranca tipoCobranca,
        Boolean ativo
) {
    public static ProdutoSummaryDto fromEntity(Produto produto) {
        return new ProdutoSummaryDto(
                produto.getId(),
                produto.getNome(),
                produto.getCategoria(),
                produto.getPreco(),
                produto.getTipoCobranca(),
                produto.getAtivo()
        );
    }
}