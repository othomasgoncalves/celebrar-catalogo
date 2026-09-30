package com.thomas.celebrarcatalog.auth;

import com.thomas.celebrarcatalog.config.SecurityConfig;
import com.thomas.celebrarcatalog.config.SessaoCookie;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, SessaoCookie.class, JwtService.class, TentativasLoginService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-bytes-de-tamanho",
        "app.jwt.expiracao=8h",
        "app.jwt.cookie-secure=false",
        "app.jwt.cookie-nome=celebrar_sessao"
})
class AuthControllerTest {

    private static final String COOKIE_SESSAO = "celebrar_sessao";
    private static final String MENSAGEM_GENERICA = "E-mail ou senha inválidos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    // --- login com sucesso ---

    @Test
    void login_com_sucesso_grava_cookie_httponly_e_devolve_os_dados_do_usuario() throws Exception {
        Usuario admin = UsuarioFixture.admin();
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(admin));

        MvcResult resultado = mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, UsuarioFixture.SENHA_ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(admin.getId().toString()))
                .andExpect(jsonPath("$.email").value(UsuarioFixture.EMAIL_ADMIN))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                // A resposta nao pode devolver o token no corpo nem vazar o hash.
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn();

        String setCookie = cookieDeSessao(resultado);
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=Strict");
        assertThat(setCookie).contains("Path=/");
        assertThat(setCookie).contains("Max-Age=" + Duration.ofHours(8).getSeconds());
        // cookie-secure=false neste perfil de teste (dev local em HTTP).
        assertThat(setCookie).doesNotContain("Secure");
    }

    @Test
    void o_cookie_gravado_no_login_contem_um_jwt_valido_com_as_claims_do_usuario() throws Exception {
        Usuario admin = UsuarioFixture.admin();
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(admin));

        MvcResult resultado = mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, UsuarioFixture.SENHA_ADMIN))
                .andExpect(status().isOk())
                .andReturn();

        UsuarioAutenticado autenticado = jwtService.validar(valorDoCookieDeSessao(resultado));
        assertThat(autenticado.id()).isEqualTo(admin.getId());
        assertThat(autenticado.email()).isEqualTo(UsuarioFixture.EMAIL_ADMIN);
        assertThat(autenticado.role()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void login_aceita_o_email_em_qualquer_caixa() throws Exception {
        when(usuarioRepository.findByEmailIgnoreCase("ADMIN@Celebrar.Local"))
                .thenReturn(Optional.of(UsuarioFixture.admin()));

        mockMvc.perform(login("ADMIN@Celebrar.Local", UsuarioFixture.SENHA_ADMIN))
                .andExpect(status().isOk());
    }

    @Test
    void login_bem_sucedido_zera_o_contador_de_falhas() throws Exception {
        Usuario comFalhas = UsuarioFixture.comFalhas(3);
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(comFalhas));

        mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, UsuarioFixture.SENHA_ADMIN))
                .andExpect(status().isOk());

        verify(usuarioRepository).save(comFalhas);
        assertThat(comFalhas.getFalhasLogin()).isZero();
        assertThat(comFalhas.getBloqueadoAte()).isNull();
    }

    // --- credenciais invalidas ---

    @Test
    void senha_errada_devolve_401_com_mensagem_generica_e_sem_cookie() throws Exception {
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(UsuarioFixture.admin()));

        MvcResult resultado = mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, "senha-errada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value(MENSAGEM_GENERICA))
                .andReturn();

        assertThat(cookieDeSessao(resultado)).isNull();
    }

    @Test
    void email_inexistente_devolve_exatamente_a_mesma_resposta_de_senha_errada() throws Exception {
        when(usuarioRepository.findByEmailIgnoreCase("ninguem@celebrar.local"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(UsuarioFixture.admin()));

        MvcResult inexistente = mockMvc.perform(login("ninguem@celebrar.local", "qualquer-senha"))
                .andExpect(status().isUnauthorized())
                .andReturn();
        MvcResult senhaErrada = mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, "senha-errada"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // Status e corpo identicos: nada distingue "esse e-mail nao existe" de
        // "a senha esta errada", o que impede enumerar os e-mails cadastrados.
        assertThat(inexistente.getResponse().getStatus())
                .isEqualTo(senhaErrada.getResponse().getStatus());
        assertThat(inexistente.getResponse().getContentAsString())
                .isEqualTo(senhaErrada.getResponse().getContentAsString())
                .contains(MENSAGEM_GENERICA);
    }

    @Test
    void conta_inativa_devolve_a_mensagem_generica_e_nao_conta_como_falha() throws Exception {
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(UsuarioFixture.inativo()));

        mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, UsuarioFixture.SENHA_ADMIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value(MENSAGEM_GENERICA));

        verify(usuarioRepository, never()).save(any());
    }

    // --- rate limit ---

    @Test
    void a_quinta_falha_grava_bloqueio_de_quinze_minutos() throws Exception {
        Usuario usuario = UsuarioFixture.comFalhas(4);
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(usuario));

        Instant antes = Instant.now();
        mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, "senha-errada"))
                .andExpect(status().isUnauthorized());

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(capturado.capture());
        assertThat(capturado.getValue().getFalhasLogin()).isEqualTo(5);
        assertThat(capturado.getValue().getBloqueadoAte())
                .isNotNull()
                .isAfterOrEqualTo(antes.plus(Duration.ofMinutes(15)).minusSeconds(5));
    }

    @Test
    void sexta_tentativa_devolve_423_sem_nem_conferir_a_senha() throws Exception {
        Usuario bloqueado = UsuarioFixture.bloqueadoAte(Instant.now().plus(Duration.ofMinutes(15)));
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(bloqueado));

        MvcResult resultado = mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, UsuarioFixture.SENHA_ADMIN))
                .andExpect(status().isLocked()) // 423
                .andExpect(jsonPath("$.erro").value("Muitas tentativas de login. Tente novamente em alguns minutos."))
                .andReturn();

        // Mesmo com a senha correta o bloqueio prevalece e nenhuma sessao e criada.
        assertThat(cookieDeSessao(resultado)).isNull();
    }

    @Test
    void bloqueio_vencido_libera_a_conta_e_zera_o_contador() throws Exception {
        Usuario usuario = UsuarioFixture.bloqueadoAte(Instant.now().minus(Duration.ofSeconds(1)));
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, UsuarioFixture.SENHA_ADMIN))
                .andExpect(status().isOk());

        assertThat(usuario.getFalhasLogin()).isZero();
        assertThat(usuario.getBloqueadoAte()).isNull();
    }

    // --- Bean Validation ---

    @Test
    void email_malformado_devolve_400_sem_consultar_o_banco() throws Exception {
        mockMvc.perform(login("nao-e-email", "alguma-senha"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());

        verify(usuarioRepository, never()).findByEmailIgnoreCase(any());
    }

    @Test
    void senha_em_branco_devolve_400() throws Exception {
        mockMvc.perform(login(UsuarioFixture.EMAIL_ADMIN, "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void corpo_ausente_devolve_400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // --- logout ---

    @Test
    void logout_limpa_o_cookie_com_max_age_zero() throws Exception {
        MvcResult resultado = mockMvc.perform(comCsrf(post("/api/auth/logout")))
                .andExpect(status().isNoContent())
                .andReturn();

        String setCookie = cookieDeSessao(resultado);
        assertThat(setCookie).isNotNull();
        assertThat(setCookie).contains("Max-Age=0");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Path=/");
    }

    @Test
    void logout_sem_header_csrf_devolve_403() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    // --- GET /api/auth/eu ---

    @Test
    void eu_sem_cookie_devolve_401_em_json_limpo() throws Exception {
        mockMvc.perform(get("/api/auth/eu"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.erro").value("Não autenticado"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("<html"))));
    }

    @Test
    void eu_com_cookie_valido_devolve_os_dados_da_sessao() throws Exception {
        Usuario admin = UsuarioFixture.admin();
        String token = jwtService.gerarToken(admin);

        mockMvc.perform(get("/api/auth/eu").cookie(new Cookie(COOKIE_SESSAO, token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(admin.getId().toString()))
                .andExpect(jsonPath("$.email").value(admin.getEmail()))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        // Autenticacao stateless: o token basta, nao ha ida ao banco por requisicao.
        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    void eu_com_cookie_invalido_devolve_401_e_manda_o_navegador_descartar_o_cookie() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/auth/eu")
                        .cookie(new Cookie(COOKIE_SESSAO, "token.completamente.invalido")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Não autenticado"))
                .andReturn();

        assertThat(cookieDeSessao(resultado)).isNotNull().contains("Max-Age=0");
    }

    @Test
    void eu_com_cookie_expirado_devolve_401_sem_estourar_excecao_para_o_cliente() throws Exception {
        // Token valido em estrutura e assinatura, mas com exp no passado.
        JwtService relogioAntigo = new JwtService(
                new com.thomas.celebrarcatalog.config.JwtProperties(
                        "segredo-de-teste-com-mais-de-32-bytes-de-tamanho",
                        Duration.ofHours(8), false, COOKIE_SESSAO),
                tools.jackson.databind.json.JsonMapper.builder().build(),
                java.time.Clock.fixed(Instant.now().minus(Duration.ofHours(9)), java.time.ZoneOffset.UTC));
        String expirado = relogioAntigo.gerarToken(UsuarioFixture.admin());

        MvcResult resultado = mockMvc.perform(get("/api/auth/eu").cookie(new Cookie(COOKIE_SESSAO, expirado)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Não autenticado"))
                .andReturn();

        assertThat(resultado.getResponse().getContentAsString()).doesNotContain("Exception");
        assertThat(cookieDeSessao(resultado)).isNotNull().contains("Max-Age=0");
    }

    // --- auxiliares ---

    private static MockHttpServletRequestBuilder login(String email, String senha) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha));
    }

    /**
     * Reproduz o fluxo que o frontend usa: o valor do cookie XSRF-TOKEN (legivel por
     * JavaScript) e devolvido no header X-XSRF-TOKEN.
     */
    private static MockHttpServletRequestBuilder comCsrf(MockHttpServletRequestBuilder requisicao) {
        String token = "token-csrf-de-teste";
        return requisicao.cookie(new Cookie("XSRF-TOKEN", token)).header("X-XSRF-TOKEN", token);
    }

    /** O Set-Cookie do cookie de sessao, ignorando o XSRF-TOKEN que vai na mesma resposta. */
    private static String cookieDeSessao(MvcResult resultado) {
        List<String> cabecalhos = resultado.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        return cabecalhos.stream()
                .filter(cabecalho -> cabecalho.startsWith(COOKIE_SESSAO + "="))
                .findFirst()
                .orElse(null);
    }

    private static String valorDoCookieDeSessao(MvcResult resultado) {
        Cookie cookie = resultado.getResponse().getCookie(COOKIE_SESSAO);
        assertThat(cookie).isNotNull();
        return cookie.getValue();
    }
}
