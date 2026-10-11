package com.digitalbanking.api.util;

/**
 * Mascaramento de dados pessoais (PII) para exibição segura em respostas da API
 * e em logs. O CPF mantém apenas os 3 últimos dígitos do corpo e os 2 dígitos
 * verificadores; o e-mail mantém no máximo os 2 primeiros caracteres do local.
 */
public final class PiiMasker {

    private static final int TAMANHO_CPF = 11;

    private PiiMasker() {
    }

    public static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != TAMANHO_CPF) {
            return cpf;
        }
        return "***.***." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    public static String maskEmail(String email) {
        if (email == null) {
            return null;
        }
        int arroba = email.indexOf('@');
        if (arroba <= 0) {
            return "***";
        }
        String local = email.substring(0, arroba);
        String dominio = email.substring(arroba);
        String prefixo = local.length() > 2 ? local.substring(0, 2) : local.substring(0, 1);
        return prefixo + "***" + dominio;
    }

}
