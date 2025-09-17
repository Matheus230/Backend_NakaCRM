package com.nakacorp.backend.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDto(
        String error,
        String message,
        int status,
        String path,
        List<String> details,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {
    public static ErrorResponseDto create(String error, String message, int status, String path) {
        return new ErrorResponseDto(error, message, status, path, null, LocalDateTime.now());
    }

    public static ErrorResponseDto createWithDetails(String error, String message, int status, String path, List<String> details) {
        return new ErrorResponseDto(error, message, status, path, details, LocalDateTime.now());
    }
}
