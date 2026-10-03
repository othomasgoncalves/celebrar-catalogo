package com.thomas.celebrarcatalog.comum;

import java.math.BigDecimal;

public final class Campos {

    private Campos() {
    }

    public static String obrigatorio(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " e obrigatorio");
        }
        return limitado(valor.strip(), campo, tamanhoMaximo);
    }

    public static String opcional(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return limitado(valor.strip(), campo, tamanhoMaximo);
    }

    public static BigDecimal precoValido(BigDecimal preco) {
        if (preco == null) {
            throw new IllegalArgumentException("preco e obrigatorio");
        }
        if (preco.signum() < 0) {
            throw new IllegalArgumentException("preco nao pode ser negativo");
        }
        if (preco.scale() > 2) {
            throw new IllegalArgumentException("preco aceita no maximo 2 casas decimais");
        }
        return preco.setScale(2, java.math.RoundingMode.UNNECESSARY);
    }

    public static int quantidadeValida(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("quantidade nao pode ser negativa");
        }
        return quantidade;
    }

    public static int ordemValida(int ordem) {
        if (ordem < 0) {
            throw new IllegalArgumentException("ordem nao pode ser negativa");
        }
        return ordem;
    }

    private static String limitado(String valor, String campo, int tamanhoMaximo) {
        if (valor.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(
                    campo + " passa de " + tamanhoMaximo + " caracteres");
        }
        return valor;
    }
}
