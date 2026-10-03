package com.thomas.celebrarcatalog.imagem;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class NomeImagem {

    public static final String REGEX =
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)";

    private static final Pattern PADRAO = Pattern.compile("^" + REGEX + "$");

    private NomeImagem() {
    }

    public static boolean valido(String nome) {
        return nome != null && PADRAO.matcher(nome).matches();
    }

    static String gerar(TipoImagem tipo) {
        return UUID.randomUUID().toString().toLowerCase(Locale.ROOT) + "." + tipo.extensao();
    }

    static Optional<TipoImagem> tipoDe(String nome) {
        int ponto = nome.lastIndexOf('.');
        if (ponto < 0) {
            return Optional.empty();
        }
        return TipoImagem.porExtensao(nome.substring(ponto + 1));
    }
}
