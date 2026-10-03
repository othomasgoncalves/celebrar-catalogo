package com.thomas.celebrarcatalog.imagem;

import java.util.Optional;

enum TipoImagem {

    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    static final int BYTES_NECESSARIOS = 12;

    private final String contentType;
    private final String extensao;

    TipoImagem(String contentType, String extensao) {
        this.contentType = contentType;
        this.extensao = extensao;
    }

    String contentType() {
        return contentType;
    }

    String extensao() {
        return extensao;
    }

    static boolean contentTypeAceito(String contentType) {
        return porContentType(contentType).isPresent();
    }

    static Optional<TipoImagem> porContentType(String contentType) {
        if (contentType == null) {
            return Optional.empty();
        }
        String tipo = contentType.split(";", 2)[0].strip().toLowerCase(java.util.Locale.ROOT);
        for (TipoImagem candidato : values()) {
            if (candidato.contentType.equals(tipo)) {
                return Optional.of(candidato);
            }
        }
        return Optional.empty();
    }

    static Optional<TipoImagem> porExtensao(String extensao) {
        for (TipoImagem candidato : values()) {
            if (candidato.extensao.equals(extensao)) {
                return Optional.of(candidato);
            }
        }
        return Optional.empty();
    }

    static Optional<TipoImagem> detectar(byte[] prefixo) {
        if (prefixo == null) {
            return Optional.empty();
        }
        if (ehJpeg(prefixo)) {
            return Optional.of(JPEG);
        }
        if (ehPng(prefixo)) {
            return Optional.of(PNG);
        }
        if (ehWebp(prefixo)) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean ehJpeg(byte[] b) {
        return b.length >= 3 && igual(b, 0, 0xFF, 0xD8, 0xFF);
    }

    private static boolean ehPng(byte[] b) {
        return b.length >= 8 && igual(b, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
    }

    private static boolean ehWebp(byte[] b) {
        return b.length >= 12
                && igual(b, 0, 'R', 'I', 'F', 'F')
                && igual(b, 8, 'W', 'E', 'B', 'P');
    }

    private static boolean igual(byte[] conteudo, int inicio, int... esperados) {
        for (int i = 0; i < esperados.length; i++) {
            if ((conteudo[inicio + i] & 0xFF) != (esperados[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }
}
