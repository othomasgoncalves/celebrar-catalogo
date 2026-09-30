package com.thomas.celebrarcatalog.auth;

import com.thomas.celebrarcatalog.config.JwtProperties;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-bytes-de-tamanho";
    private static final String OUTRO_SEGREDO = "outro-segredo-de-teste-com-mais-de-32-bytes!!";

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final Instant agora = Instant.parse("2026-09-29T12:00:00Z");
    private final Clock clock = Clock.fixed(agora, ZoneOffset.UTC);

    private JwtService servico(String segredo, Clock clock) {
        return new JwtService(
                new JwtProperties(segredo, Duration.ofHours(8), false, "celebrar_sessao"),
                objectMapper,
                clock);
    }

    private JwtService servico() {
        return servico(SEGREDO, clock);
    }

    @Test
    void gera_e_valida_token_preservando_as_claims() {
        Usuario admin = UsuarioFixture.admin();
        JwtService servico = servico();

        UsuarioAutenticado autenticado = servico.validar(servico.gerarToken(admin));

        assertThat(autenticado.id()).isEqualTo(admin.getId());
        assertThat(autenticado.email()).isEqualTo(admin.getEmail());
        assertThat(autenticado.role()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void token_gerado_usa_alg_HS256_e_expira_em_oito_horas() {
        String token = servico().gerarToken(UsuarioFixture.admin());
        String[] partes = token.split("\\.");

        var cabecalho = objectMapper.readTree(Base64.getUrlDecoder().decode(partes[0]));
        var corpo = objectMapper.readTree(Base64.getUrlDecoder().decode(partes[1]));

        assertThat(cabecalho.get("alg").asString()).isEqualTo("HS256");
        assertThat(cabecalho.get("typ").asString()).isEqualTo("JWT");
        assertThat(corpo.get("iat").asLong()).isEqualTo(agora.getEpochSecond());
        assertThat(corpo.get("exp").asLong())
                .isEqualTo(agora.plus(Duration.ofHours(8)).getEpochSecond());
        assertThat(corpo.get("jti").asString()).isNotBlank();
    }

    @Test
    void recusa_token_com_alg_none_mesmo_com_o_restante_bem_formado() {
        String token = tokenForjado("{\"alg\":\"none\",\"typ\":\"JWT\"}", corpoValido(), "");

        assertThatThrownBy(() -> servico().validar(token))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("alg 'none'");
    }

    @Test
    void recusa_algoritmo_diferente_de_HS256_ainda_que_a_assinatura_HMAC_esteja_correta() {
        // Cabecalho anuncia HS512, mas a assinatura foi calculada com HmacSHA256 e o
        // segredo certo: o token so e recusado porque o alg e conferido separadamente.
        String cabecalho = "{\"alg\":\"HS512\",\"typ\":\"JWT\"}";
        String conteudo = B64.encodeToString(cabecalho.getBytes(StandardCharsets.UTF_8))
                + "." + B64.encodeToString(corpoValido().getBytes(StandardCharsets.UTF_8));
        String token = conteudo + "." + B64.encodeToString(hmacSha256(conteudo, SEGREDO));

        assertThatThrownBy(() -> servico().validar(token))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("algoritmo nao suportado: HS512");
    }

    @Test
    void recusa_token_assinado_com_outro_segredo() {
        String tokenIntruso = servico(OUTRO_SEGREDO, clock).gerarToken(UsuarioFixture.admin());

        assertThatThrownBy(() -> servico().validar(tokenIntruso))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("assinatura invalida");
    }

    @Test
    void recusa_token_com_o_corpo_alterado() {
        String token = servico().gerarToken(UsuarioFixture.admin());
        String[] partes = token.split("\\.");
        String corpoTrocado = B64.encodeToString(
                ("{\"sub\":\"" + UUID.randomUUID() + "\",\"email\":\"intruso@x.com\","
                        + "\"role\":\"ROLE_ADMIN\",\"iat\":" + agora.getEpochSecond()
                        + ",\"exp\":" + agora.plusSeconds(3600).getEpochSecond()
                        + ",\"jti\":\"" + UUID.randomUUID() + "\"}").getBytes(StandardCharsets.UTF_8));
        String adulterado = partes[0] + "." + corpoTrocado + "." + partes[2];

        assertThatThrownBy(() -> servico().validar(adulterado))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("assinatura invalida");
    }

    @Test
    void recusa_token_expirado() {
        String token = servico().gerarToken(UsuarioFixture.admin());
        Clock depoisDoPrazo = Clock.fixed(agora.plus(Duration.ofHours(8)).plusSeconds(1), ZoneOffset.UTC);

        assertThatThrownBy(() -> servico(SEGREDO, depoisDoPrazo).validar(token))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void aceita_token_no_ultimo_segundo_de_validade() {
        String token = servico().gerarToken(UsuarioFixture.admin());
        Clock umSegundoAntes = Clock.fixed(agora.plus(Duration.ofHours(8)).minusSeconds(1), ZoneOffset.UTC);

        assertThat(servico(SEGREDO, umSegundoAntes).validar(token)).isNotNull();
    }

    @Test
    void recusa_role_sem_o_prefixo_ROLE() {
        String corpo = "{\"sub\":\"" + UUID.randomUUID() + "\",\"email\":\"a@b.com\","
                + "\"role\":\"ADMIN\",\"iat\":" + agora.getEpochSecond()
                + ",\"exp\":" + agora.plusSeconds(3600).getEpochSecond()
                + ",\"jti\":\"" + UUID.randomUUID() + "\"}";

        assertThatThrownBy(() -> servico().validar(assinado(corpo)))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("prefixo ROLE_");
    }

    @Test
    void recusa_token_sem_claim_exp() {
        String corpo = "{\"sub\":\"" + UUID.randomUUID() + "\",\"email\":\"a@b.com\","
                + "\"role\":\"ROLE_ADMIN\",\"iat\":" + agora.getEpochSecond()
                + ",\"jti\":\"" + UUID.randomUUID() + "\"}";

        assertThatThrownBy(() -> servico().validar(assinado(corpo)))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("'exp' ausente");
    }

    @Test
    void recusa_sub_que_nao_e_uuid() {
        String corpo = "{\"sub\":\"nao-e-uuid\",\"email\":\"a@b.com\",\"role\":\"ROLE_ADMIN\","
                + "\"iat\":" + agora.getEpochSecond()
                + ",\"exp\":" + agora.plusSeconds(3600).getEpochSecond()
                + ",\"jti\":\"" + UUID.randomUUID() + "\"}";

        assertThatThrownBy(() -> servico().validar(assinado(corpo)))
                .isInstanceOf(JwtInvalidoException.class)
                .hasMessageContaining("'sub' nao e um UUID");
    }

    @Test
    void recusa_tokens_estruturalmente_invalidos() {
        JwtService servico = servico();

        assertThatThrownBy(() -> servico.validar(null)).isInstanceOf(JwtInvalidoException.class);
        assertThatThrownBy(() -> servico.validar("  ")).isInstanceOf(JwtInvalidoException.class);
        assertThatThrownBy(() -> servico.validar("a.b")).isInstanceOf(JwtInvalidoException.class);
        assertThatThrownBy(() -> servico.validar("a.b.c.d")).isInstanceOf(JwtInvalidoException.class);
        assertThatThrownBy(() -> servico.validar("!!!.!!!.!!!")).isInstanceOf(JwtInvalidoException.class);
    }

    // --- auxiliares ---

    private String corpoValido() {
        return "{\"sub\":\"" + UUID.randomUUID() + "\",\"email\":\"a@b.com\","
                + "\"role\":\"ROLE_ADMIN\",\"iat\":" + agora.getEpochSecond()
                + ",\"exp\":" + agora.plusSeconds(3600).getEpochSecond()
                + ",\"jti\":\"" + UUID.randomUUID() + "\"}";
    }

    /** Monta um token com cabecalho HS256 valido e assinatura correta sobre o corpo dado. */
    private String assinado(String corpo) {
        return tokenAssinado("{\"alg\":\"HS256\",\"typ\":\"JWT\"}", corpo);
    }

    private String tokenAssinado(String cabecalho, String corpo) {
        String conteudo = B64.encodeToString(cabecalho.getBytes(StandardCharsets.UTF_8))
                + "." + B64.encodeToString(corpo.getBytes(StandardCharsets.UTF_8));
        return conteudo + "." + B64.encodeToString(hmacSha256(conteudo, SEGREDO));
    }

    private static String tokenForjado(String cabecalho, String corpo, String assinatura) {
        return B64.encodeToString(cabecalho.getBytes(StandardCharsets.UTF_8))
                + "." + B64.encodeToString(corpo.getBytes(StandardCharsets.UTF_8))
                + "." + assinatura;
    }

    private static byte[] hmacSha256(String conteudo, String segredo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(conteudo.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception excecao) {
            throw new IllegalStateException(excecao);
        }
    }
}
