package com.nakacorp.backend.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ApiResponseDto<T>(
        boolean success,
        String message,
        T data,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(true, "Operação realizada com sucesso", data, LocalDateTime.now());
    }

    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, message, null, LocalDateTime.now());
    }
}
