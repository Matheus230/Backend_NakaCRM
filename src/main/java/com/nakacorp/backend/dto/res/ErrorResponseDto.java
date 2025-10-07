package com.nakacorp.backend.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO padronizado para respostas de erro.
 *
 * @param timestamp Data/hora do erro
 * @param status Código HTTP
 * @param error Tipo do erro
 * @param message Mensagem descritiva
 * @param path URI da requisição
 * @param details Lista opcional de detalhes adicionais
 */
public record ErrorResponseDto(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,

        int status,

        String error,

        String message,

        String path,

        List<String> details
) {
    /**
     * Factory method para criação sem detalhes.
     */
    public static ErrorResponseDto create(String error, String message, int status, String path) {
        return new ErrorResponseDto(LocalDateTime.now(), status, error, message, path, null);
    }

    /**
     * Factory method para criação com detalhes.
     */
    public static ErrorResponseDto createWithDetails(
            String error,
            String message,
            int status,
            String path,
            List<String> details) {
        return new ErrorResponseDto(LocalDateTime.now(), status, error, message, path, details);
    }
}