package com.nakacorp.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando há tentativa de criar um recurso que já existe.
 * <p>
 * Retorna HTTP 409 Conflict automaticamente.
 * Exemplo: tentativa de cadastrar email duplicado.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s já existe com %s: '%s'", resourceName, fieldName, fieldValue),
            "DUPLICATE_RESOURCE");
    }

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }
}
