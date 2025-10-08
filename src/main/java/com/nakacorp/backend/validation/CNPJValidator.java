package com.nakacorp.backend.validation;

import br.com.caelum.stella.validation.InvalidStateException;

/**
 * Utilitário para validação de CNPJ (Cadastro Nacional de Pessoa Jurídica).
 * <p>
 * Usa a biblioteca Caelum Stella para validação completa do CNPJ,
 * incluindo dígitos verificadores.
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
public class CNPJValidator {

    private static final br.com.caelum.stella.validation.CNPJValidator validator =
        new br.com.caelum.stella.validation.CNPJValidator();

    /**
     * Valida se um CNPJ é válido.
     *
     * @param cnpj CNPJ a ser validado (com ou sem formatação)
     * @return true se o CNPJ é válido, false caso contrário
     */
    public static boolean isValid(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }

        try {
            validator.assertValid(cnpj);
            return true;
        } catch (InvalidStateException e) {
            return false;
        }
    }

    /**
     * Remove formatação do CNPJ (pontos, hífens e barras).
     *
     * @param cnpj CNPJ formatado
     * @return CNPJ apenas com dígitos
     */
    public static String unformat(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        return cnpj.replaceAll("[^\\d]", "");
    }

    /**
     * Formata um CNPJ no padrão XX.XXX.XXX/XXXX-XX.
     *
     * @param cnpj CNPJ sem formatação (apenas dígitos)
     * @return CNPJ formatado
     */
    public static String format(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return cnpj;
        }

        return cnpj.substring(0, 2) + "." +
               cnpj.substring(2, 5) + "." +
               cnpj.substring(5, 8) + "/" +
               cnpj.substring(8, 12) + "-" +
               cnpj.substring(12, 14);
    }
}
