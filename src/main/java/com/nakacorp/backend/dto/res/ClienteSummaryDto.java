package com.nakacorp.backend.dto.res;

import com.nakacorp.backend.model.Cliente;
import com.nakacorp.backend.model.enums.OrigemLead;
import com.nakacorp.backend.model.enums.StatusLead;

public record ClienteSummaryDto(
        Long id,
        String nome,
        String email,
        String empresa,
        StatusLead statusLead,
        OrigemLead origemLead
) {
    public static ClienteSummaryDto fromEntity(Cliente cliente) {
        return new ClienteSummaryDto(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getEmpresa(),
                cliente.getStatusLead(),
                cliente.getOrigemLead()
        );
    }
}
