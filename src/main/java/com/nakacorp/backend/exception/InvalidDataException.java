package com.nakacorp.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando dados inválidos são fornecidos.
 * <p>
 * Retorna HTTP 400 Bad Request automaticamente.
 * Exemplo: CPF/CNPJ inválido, formato de telefone incorreto, etc.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDataException extends BusinessException {

    public InvalidDataException(String message) {
        super(message, "INVALID_DATA");
    }

    public InvalidDataException(String fieldName, String reason) {
        super(String.format("Dados inválidos no campo '%s': %s", fieldName, reason),
            "INVALID_DATA");
    }
}
