package com.thomas.celebrarcatalog.imagem;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("app.imagens")
public record ImagemProperties(Path diretorio) {

    private static final Path DIRETORIO_PADRAO = Path.of("dados", "imagens");

    public ImagemProperties {
        if (diretorio == null || diretorio.toString().isBlank()) {
            diretorio = DIRETORIO_PADRAO;
        }
        diretorio = diretorio.toAbsolutePath().normalize();
    }
}
