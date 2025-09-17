package com.nakacorp.backend.dto.res;

public record LoginResponseDto(
        String token,
        String tipo,
        Long expiresIn,
        UsuarioResponseDto usuario
) {
    public static LoginResponseDto create(String token, Long expiresIn, UsuarioResponseDto usuario) {
        return new LoginResponseDto(token, "Bearer", expiresIn, usuario);
    }
}