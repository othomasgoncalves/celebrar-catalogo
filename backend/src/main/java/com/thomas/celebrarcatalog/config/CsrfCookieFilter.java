package com.thomas.celebrarcatalog.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao,
                                    HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {
        CsrfToken token = (CsrfToken) requisicao.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            token.getToken();
        }
        cadeia.doFilter(requisicao, resposta);
    }
}
