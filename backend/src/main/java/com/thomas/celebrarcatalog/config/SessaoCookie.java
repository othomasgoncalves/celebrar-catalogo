package com.thomas.celebrarcatalog.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SessaoCookie {

    private final String nome;
    private final boolean secure;
    private final Duration duracao;

    SessaoCookie(JwtProperties propriedades) {
        this.nome = propriedades.cookieNome();
        this.secure = propriedades.cookieSecure();
        this.duracao = propriedades.expiracao();
    }

    public String nome() {
        return nome;
    }

    public void gravar(HttpServletResponse resposta, String token) {
        resposta.addHeader(HttpHeaders.SET_COOKIE, base(token).maxAge(duracao).build().toString());
    }

    public void limpar(HttpServletResponse resposta) {
        resposta.addHeader(HttpHeaders.SET_COOKIE, base("").maxAge(0).build().toString());
    }

    public String lerToken(HttpServletRequest requisicao) {
        if (requisicao.getCookies() == null) {
            return null;
        }
        for (var cookie : requisicao.getCookies()) {
            if (nome.equals(cookie.getName())) {
                String valor = cookie.getValue();
                return valor == null || valor.isBlank() ? null : valor;
            }
        }
        return null;
    }

    private ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(nome, valor)
                .httpOnly(true)      
                .secure(secure)      
                .sameSite("Strict")
                .path("/");
    }
}
