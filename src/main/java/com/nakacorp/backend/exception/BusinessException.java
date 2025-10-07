package com.nakacorp.backend.exception;

/**
 * Exceção base para erros de negócio da aplicação.
 * <p>
 * Todas as exceções de negócio devem estender esta classe para tratamento
 * consistente no {@link com.nakacorp.backend.controller.GlobalExceptionHandler}.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
public class BusinessException extends RuntimeException {

    /**
     * Código de erro específico para facilitar tratamento no frontend
     */
    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
