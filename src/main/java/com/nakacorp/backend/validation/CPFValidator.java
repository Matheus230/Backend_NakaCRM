package com.nakacorp.backend.validation;

import br.com.caelum.stella.validation.InvalidStateException;

/**
 * Utilitário para validação de CPF (Cadastro de Pessoa Física).
 * <p>
 * Usa a biblioteca Caelum Stella para validação completa do CPF,
 * incluindo dígitos verificadores.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
public class CPFValidator {

    private static final br.com.caelum.stella.validation.CPFValidator validator =
        new br.com.caelum.stella.validation.CPFValidator();

    /**
     * Valida se um CPF é válido.
     *
     * @param cpf CPF a ser validado (com ou sem formatação)
     * @return true se o CPF é válido, false caso contrário
     */
    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return false;
        }

        try {
            validator.assertValid(cpf);
            return true;
        } catch (InvalidStateException e) {
            return false;
        }
    }

    /**
     * Remove formatação do CPF (pontos e hífens).
     *
     * @param cpf CPF formatado
     * @return CPF apenas com dígitos
     */
    public static String unformat(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("[^\\d]", "");
    }

    /**
     * Formata um CPF no padrão XXX.XXX.XXX-XX.
     *
     * @param cpf CPF sem formatação (apenas dígitos)
     * @return CPF formatado
     */
    public static String format(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }

        return cpf.substring(0, 3) + "." +
               cpf.substring(3, 6) + "." +
               cpf.substring(6, 9) + "-" +
               cpf.substring(9, 11);
    }
}
