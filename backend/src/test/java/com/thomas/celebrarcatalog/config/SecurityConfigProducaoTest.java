package com.thomas.celebrarcatalog.config;

import com.thomas.celebrarcatalog.auth.JwtService;
import com.thomas.celebrarcatalog.auth.TentativasLoginService;
import com.thomas.celebrarcatalog.auth.UsuarioFixture;
import com.thomas.celebrarcatalog.auth.UsuarioRepository;
import com.thomas.celebrarcatalog.categoria.CategoriaRepository;
import com.thomas.celebrarcatalog.cesta.CestaProntaRepository;
import com.thomas.celebrarcatalog.produto.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Mesma configuracao com {@code app.jwt.cookie-secure=true}, o perfil de producao:
 * o cookie passa a exigir HTTPS e o HSTS entra em cena.
 */
@WebMvcTest
@Import({SecurityConfig.class, SessaoCookie.class, JwtService.class, TentativasLoginService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-bytes-de-tamanho",
        "app.jwt.cookie-secure=true",
        "app.jwt.cookie-nome=celebrar_sessao"
})
class SecurityConfigProducaoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRepository usuarioRepository;
    @MockitoBean
    private ProdutoRepository produtoRepository;
    @MockitoBean
    private CategoriaRepository categoriaRepository;
    @MockitoBean
    private CestaProntaRepository cestaProntaRepository;

    @Test
    void cookie_de_sessao_ganha_o_atributo_secure() throws Exception {
        when(usuarioRepository.findByEmailIgnoreCase(UsuarioFixture.EMAIL_ADMIN))
                .thenReturn(Optional.of(UsuarioFixture.admin()));

        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .secure(true)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}"
                                .formatted(UsuarioFixture.EMAIL_ADMIN, UsuarioFixture.SENHA_ADMIN)))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = resultado.getResponse().getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(cabecalho -> cabecalho.startsWith("celebrar_sessao="))
                .findFirst()
                .orElseThrow();

        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=Strict");
    }

    @Test
    void hsts_e_enviado_em_https() throws Exception {
        mockMvc.perform(get("/api/produtos").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().string("Strict-Transport-Security",
                        "max-age=31536000 ; includeSubDomains"));
    }
}
