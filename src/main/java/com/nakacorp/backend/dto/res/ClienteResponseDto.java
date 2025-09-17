package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.Cliente;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;

import java.time.LocalDateTime;

public record ClienteResponseDto(
        Long id,
        String nome,
        String email,
        String telefone,
        String endereco,
        String cidade,
        String estado,
        String cep,
        String empresa,
        String cargo,
        OrigemLead origemLead,
        StatusLead statusLead,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime dataPrimeiroContato,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime dataUltimaInteracao,

        String observacoes,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt
) {
    public static ClienteResponseDto fromEntity(Cliente cliente) {
        return new ClienteResponseDto(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getEndereco(),
                cliente.getCidade(),
                cliente.getEstado(),
                cliente.getCep(),
                cliente.getEmpresa(),
                cliente.getCargo(),
                cliente.getOrigemLead(),
                cliente.getStatusLead(),
                cliente.getDataPrimeiroContato(),
                cliente.getDataUltimaInteracao(),
                cliente.getObservacoes(),
                cliente.getCreatedAt(),
                cliente.getUpdatedAt()
        );
    }
}

