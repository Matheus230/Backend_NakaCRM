package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.TipoCobranca;
import com.nakacorp.backend.model.enums.TipoPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoUpdateDto(
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        String descricao,

        @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
        String categoria,

        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal preco,

        Boolean pago,

        TipoPagamento tipoPagamento,

        TipoCobranca tipoCobranca,

        Boolean ativo
) {}