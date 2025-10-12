package com.nakacorp.backend.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de captação de lead via formulário público.
 * <p>
 * Este DTO é utilizado quando um potencial cliente preenche um formulário
 * durante uma palestra, evento ou landing page, demonstrando interesse
 * em produtos/serviços da empresa.
 * </p>
 *
 * @param nome Nome completo do lead
 * @param email Email para contato
 * @param telefone Telefone para contato (WhatsApp, celular, etc)
 * @param empresa Nome da empresa onde trabalha (opcional)
 * @param cargo Cargo/função na empresa (opcional)
 * @param formaContatoPreferida Forma preferida de contato (EMAIL, TELEFONE, WHATSAPP)
 * @param observacoes Observações adicionais do lead (opcional)
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 2025-01-12
 */
public record LeadCaptureRequestDto(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,

        @NotBlank(message = "Telefone é obrigatório")
        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Size(max = 255, message = "Empresa deve ter no máximo 255 caracteres")
        String empresa,

        @Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
        String cargo,

        @NotBlank(message = "Forma de contato preferida é obrigatória")
        String formaContatoPreferida,

        String observacoes
) {}
