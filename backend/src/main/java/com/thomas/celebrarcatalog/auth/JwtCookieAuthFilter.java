package com.thomas.celebrarcatalog.auth;

import com.thomas.celebrarcatalog.config.SessaoCookie;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtCookieAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SessaoCookie sessaoCookie;

    public JwtCookieAuthFilter(JwtService jwtService, SessaoCookie sessaoCookie) {
        this.jwtService = jwtService;
        this.sessaoCookie = sessaoCookie;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao,
                                    HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            autenticar(requisicao, resposta);
        }
        cadeia.doFilter(requisicao, resposta);
    }

    private void autenticar(HttpServletRequest requisicao, HttpServletResponse resposta) {
        String token = sessaoCookie.lerToken(requisicao);
        if (token == null) {
            return;
        }
        try {
            UsuarioAutenticado usuario = jwtService.validar(token);

            var autenticacao = UsernamePasswordAuthenticationToken.authenticated(
                    usuario, null, List.of(new SimpleGrantedAuthority(usuario.role())));
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacao);
            SecurityContextHolder.setContext(contexto);
        } catch (JwtInvalidoException excecao) {
            logger.debug("Cookie de sessao recusado: " + excecao.getMessage());
            sessaoCookie.limpar(resposta);
        }
    }
}
