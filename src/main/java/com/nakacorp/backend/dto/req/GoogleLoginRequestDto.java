package com.nakacorp.backend.dto.req;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de login com Google OAuth2.
 *
 * @param idToken Token ID do Google OAuth2 recebido do frontend
 */
public record GoogleLoginRequestDto(
        @NotBlank(message = "Token do Google é obrigatório")
        String idToken
) {}
