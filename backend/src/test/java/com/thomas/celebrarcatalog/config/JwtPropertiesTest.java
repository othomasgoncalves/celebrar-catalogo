package com.thomas.celebrarcatalog.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O segredo do JWT e requisito de inicializacao: sem ele, ou com um segredo curto
 * demais para HS256, a aplicacao precisa se recusar a subir em vez de assinar tokens
 * com uma chave fraca.
 */
class JwtPropertiesTest {

    private static final String SEGREDO_VALIDO = "segredo-de-teste-com-mais-de-32-bytes-de-tamanho";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(ConfiguracaoDeTeste.class);

    @Test
    void sobe_com_segredo_valido_e_aplica_os_padroes() {
        runner.withPropertyValues("app.jwt.secret=" + SEGREDO_VALIDO).run(contexto -> {
            assertThat(contexto).hasNotFailed();
            JwtProperties propriedades = contexto.getBean(JwtProperties.class);
            assertThat(propriedades.expiracao()).isEqualTo(Duration.ofHours(8));
            assertThat(propriedades.cookieNome()).isEqualTo("celebrar_sessao");
            assertThat(propriedades.cookieSecure()).isFalse();
        });
    }

    @Test
    void falha_a_inicializacao_quando_o_segredo_esta_ausente() {
        runner.run(contexto -> assertThat(contexto)
                .hasFailed()
                .getFailure()
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.jwt.secret nao foi configurado")
                .hasMessageContaining("APP_JWT_SECRET"));
    }

    @Test
    void falha_a_inicializacao_quando_o_segredo_esta_em_branco() {
        runner.withPropertyValues("app.jwt.secret=   ").run(contexto -> assertThat(contexto)
                .hasFailed()
                .getFailure()
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nao foi configurado"));
    }

    @Test
    void falha_a_inicializacao_quando_o_segredo_tem_menos_de_32_bytes() {
        String curto = "a".repeat(31);
        assertThat(curto.getBytes(StandardCharsets.UTF_8)).hasSize(31);

        runner.withPropertyValues("app.jwt.secret=" + curto).run(contexto -> assertThat(contexto)
                .hasFailed()
                .getFailure()
                .rootCause()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("apenas 31 bytes")
                .hasMessageContaining("minimo e 32"));
    }

    @Test
    void o_limite_e_em_bytes_e_nao_em_caracteres() {
        // 16 caracteres acentuados = 32 bytes em UTF-8: passa raspando.
        String trintaEDoisBytes = "á".repeat(16);
        assertThat(trintaEDoisBytes.getBytes(StandardCharsets.UTF_8)).hasSize(32);
        assertThat(trintaEDoisBytes).hasSize(16);

        runner.withPropertyValues("app.jwt.secret=" + trintaEDoisBytes)
                .run(contexto -> assertThat(contexto).hasNotFailed());

        // 15 acentuados = 30 bytes: nao passa, mesmo tendo "quase" o mesmo tamanho.
        runner.withPropertyValues("app.jwt.secret=" + "á".repeat(15))
                .run(contexto -> assertThat(contexto).hasFailed());
    }

    @Test
    void expiracao_zero_ou_negativa_cai_no_padrao_de_oito_horas() {
        assertThat(new JwtProperties(SEGREDO_VALIDO, Duration.ZERO, false, null).expiracao())
                .isEqualTo(Duration.ofHours(8));
        assertThat(new JwtProperties(SEGREDO_VALIDO, Duration.ofMinutes(-5), false, null).expiracao())
                .isEqualTo(Duration.ofHours(8));
    }

    @Test
    void construtor_rejeita_segredo_invalido_diretamente() {
        assertThatThrownBy(() -> new JwtProperties(null, null, false, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProperties("curto", null, false, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chave_hmac_usa_os_bytes_utf8_do_segredo() {
        var chave = new JwtProperties(SEGREDO_VALIDO, null, false, null).chaveHmac();

        assertThat(chave.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(chave.getEncoded()).isEqualTo(SEGREDO_VALIDO.getBytes(StandardCharsets.UTF_8));
    }

    @org.springframework.boot.context.properties.EnableConfigurationProperties(JwtProperties.class)
    static class ConfiguracaoDeTeste {
    }
}
