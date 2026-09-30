package com.thomas.celebrarcatalog.config;

import com.thomas.celebrarcatalog.auth.JwtService;
import com.thomas.celebrarcatalog.auth.UsuarioFixture;
import com.thomas.celebrarcatalog.auth.TentativasLoginService;
import com.thomas.celebrarcatalog.auth.UsuarioRepository;
import com.thomas.celebrarcatalog.categoria.CategoriaRepository;
import com.thomas.celebrarcatalog.cesta.CestaProntaRepository;
import com.thomas.celebrarcatalog.produto.ProdutoRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica o mapa de autorizacao, o CSRF e os headers de resposta.
 *
 * <p>Nao existe controller sob {@code /api/admin/**} nesta fase do projeto, e isso nao
 * atrapalha: os filtros de seguranca rodam antes do mapeamento de handlers, entao 401 e
 * 403 sao decididos sem nunca chegar a um controller. Quando a requisicao <i>passa</i>
 * pela seguranca, o resultado esperado e 404 — prova de que a autorizacao liberou.
 */
@WebMvcTest
@Import({SecurityConfig.class, SessaoCookie.class, JwtService.class, TentativasLoginService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-bytes-de-tamanho",
        "app.jwt.cookie-secure=false",
        "app.jwt.cookie-nome=celebrar_sessao"
})
class SecurityConfigTest {

    private static final String COOKIE_SESSAO = "celebrar_sessao";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;
    @MockitoBean
    private ProdutoRepository produtoRepository;
    @MockitoBean
    private CategoriaRepository categoriaRepository;
    @MockitoBean
    private CestaProntaRepository cestaProntaRepository;

    // --- endpoints publicos ---

    @Test
    void get_produtos_continua_publico() throws Exception {
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk());
    }

    @Test
    void get_produtos_com_filtro_continua_publico() throws Exception {
        mockMvc.perform(get("/api/produtos").param("disponivelNaCesta", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void get_categorias_e_get_cestas_continuam_publicos() throws Exception {
        mockMvc.perform(get("/api/categorias")).andExpect(status().isOk());
        mockMvc.perform(get("/api/cestas")).andExpect(status().isOk());
    }

    @Test
    void get_imagens_e_publico() throws Exception {
        // Nao ha controller de imagens ainda: 404 (e nao 401/403) prova que a
        // seguranca liberou a rota.
        mockMvc.perform(get("/api/imagens/produto-1.png"))
                .andExpect(status().isNotFound());
    }

    // --- /api/admin/** ---

    @Test
    void admin_sem_cookie_devolve_401_em_json() throws Exception {
        mockMvc.perform(get("/api/admin/produtos"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.erro").value("Não autenticado"));
    }

    @Test
    void admin_com_cookie_mas_sem_header_csrf_devolve_403() throws Exception {
        String token = jwtService.gerarToken(UsuarioFixture.admin());

        mockMvc.perform(post("/api/admin/produtos")
                        .cookie(new Cookie(COOKIE_SESSAO, token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    void admin_com_cookie_e_header_csrf_passa_pela_seguranca() throws Exception {
        String token = jwtService.gerarToken(UsuarioFixture.admin());

        // 404 porque o endpoint ainda nao existe — o que importa e nao ser 401 nem 403.
        mockMvc.perform(comCsrf(post("/api/admin/produtos"))
                        .cookie(new Cookie(COOKIE_SESSAO, token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void admin_com_sessao_de_role_diferente_devolve_403() throws Exception {
        String token = jwtService.gerarToken(UsuarioFixture.comRole("ROLE_CLIENTE"));

        mockMvc.perform(get("/api/admin/produtos").cookie(new Cookie(COOKIE_SESSAO, token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }

    @Test
    void admin_com_cookie_adulterado_devolve_401() throws Exception {
        String token = jwtService.gerarToken(UsuarioFixture.admin());
        String adulterado = token.substring(0, token.length() - 4) + "AAAA";

        mockMvc.perform(get("/api/admin/produtos").cookie(new Cookie(COOKIE_SESSAO, adulterado)))
                .andExpect(status().isUnauthorized());
    }

    // --- denyAll ---

    @Test
    void rota_fora_do_mapa_cai_no_denyAll() throws Exception {
        // POST /api/produtos nao esta liberado (so o GET esta).
        mockMvc.perform(comCsrf(post("/api/produtos"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(comCsrf(put("/api/cestas/1"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/qualquer-coisa-nova"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void metodo_nao_liberado_permanece_negado_mesmo_com_sessao_de_admin() throws Exception {
        String token = jwtService.gerarToken(UsuarioFixture.admin());

        // ROLE_ADMIN nao abre rotas fora de /api/admin/**: o denyAll final vale para todos.
        mockMvc.perform(comCsrf(post("/api/produtos")).cookie(new Cookie(COOKIE_SESSAO, token))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    // --- headers de seguranca ---

    @Test
    void headers_de_seguranca_acompanham_as_respostas() throws Exception {
        mockMvc.perform(get("/api/produtos"))
                .andExpect(header().string("Content-Security-Policy",
                        "default-src 'self'; img-src 'self' data:; script-src 'self'; "
                                + "style-src 'self' 'unsafe-inline'; frame-ancestors 'none'; "
                                + "object-src 'none'; base-uri 'self'"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=()"));
    }

    @Test
    void hsts_fica_desligado_quando_cookie_secure_e_false() throws Exception {
        mockMvc.perform(get("/api/produtos").secure(true))
                .andExpect(header().doesNotExist("Strict-Transport-Security"));
    }

    @Test
    void respostas_de_erro_nao_trazem_html_nem_stack_trace() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/admin/produtos"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String corpo = resultado.getResponse().getContentAsString();
        assertThat(corpo).doesNotContain("<html").doesNotContain("<!DOCTYPE");
        assertThat(corpo).doesNotContain("Exception").doesNotContain("at org.springframework");
        assertThat(corpo).isEqualTo("{\"erro\":\"Não autenticado\"}");
    }

    // --- CSRF ---

    @Test
    void o_cookie_xsrf_token_e_enviado_para_o_frontend_poder_devolve_lo() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/produtos")).andReturn();

        Cookie csrf = resultado.getResponse().getCookie("XSRF-TOKEN");
        assertThat(csrf).isNotNull();
        assertThat(csrf.getValue()).isNotBlank();
        // Precisa ser legivel por JavaScript para o frontend copia-lo no header.
        assertThat(csrf.isHttpOnly()).isFalse();
    }

    @Test
    void csrf_com_header_divergente_do_cookie_devolve_403() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("XSRF-TOKEN", "valor-do-cookie"))
                        .header("X-XSRF-TOKEN", "valor-diferente"))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_e_isento_de_csrf() throws Exception {
        // Sem cookie e sem header CSRF: precisa passar do CsrfFilter e chegar ao
        // controller, que responde 401 por credencial invalida (nao 403).
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ninguem@celebrar.local\",\"senha\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void gets_publicos_nao_exigem_csrf() throws Exception {
        mockMvc.perform(get("/api/produtos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/categorias")).andExpect(status().isOk());
    }

    private static MockHttpServletRequestBuilder comCsrf(MockHttpServletRequestBuilder requisicao) {
        String token = "token-csrf-de-teste";
        return requisicao.cookie(new Cookie("XSRF-TOKEN", token)).header("X-XSRF-TOKEN", token);
    }
}
