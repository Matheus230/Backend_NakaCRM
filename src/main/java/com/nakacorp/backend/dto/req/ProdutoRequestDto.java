package com.nakacorp.backend.dto.req;

import com.nakacorp.backend.model.enums.TipoCobranca;
import com.nakacorp.backend.model.enums.TipoPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequestDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        String descricao,

        @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
        String categoria,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
        BigDecimal preco,

        Boolean pago,

        TipoPagamento tipoPagamento,

        TipoCobranca tipoCobranca,

        Boolean ativo
) {
    public ProdutoRequestDto {
        if (tipoCobranca == null) tipoCobranca = TipoCobranca.UNICO;
        if (ativo == null) ativo = true;
    }
}

