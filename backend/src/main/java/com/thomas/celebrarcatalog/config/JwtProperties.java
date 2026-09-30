package com.thomas.celebrarcatalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, Duration expiracao, boolean cookieSecure, String cookieNome) {

    private static final int MINIMO_BYTES_SEGREDO = 32;

    private static final Duration EXPIRACAO_PADRAO = Duration.ofHours(8);
    private static final String COOKIE_NOME_PADRAO = "celebrar_sessao";

    private static final String AJUDA = """
            Defina a variavel de ambiente APP_JWT_SECRET com no minimo %d bytes \
            (256 bits, exigencia do HS256) antes de iniciar a aplicacao. \
            Exemplo para gerar um segredo: openssl rand -base64 48. \
            Nao versione esse valor no repositorio.""".formatted(MINIMO_BYTES_SEGREDO);

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret nao foi configurado. " + AJUDA);
        }
        int bytes = secret.getBytes(StandardCharsets.UTF_8).length;
        if (bytes < MINIMO_BYTES_SEGREDO) {
            throw new IllegalStateException(
                    "app.jwt.secret tem apenas %d bytes, e o minimo e %d. %s"
                            .formatted(bytes, MINIMO_BYTES_SEGREDO, AJUDA));
        }
        if (expiracao == null || expiracao.isZero() || expiracao.isNegative()) {
            expiracao = EXPIRACAO_PADRAO;
        }
        if (cookieNome == null || cookieNome.isBlank()) {
            cookieNome = COOKIE_NOME_PADRAO;
        }
    }

    public SecretKeySpec chaveHmac() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
