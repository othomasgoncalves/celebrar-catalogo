package com.thomas.celebrarcatalog.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class RespostasSeguranca {

    private static final String CORPO_NAO_AUTENTICADO = "{\"erro\":\"Não autenticado\"}";
    private static final String CORPO_ACESSO_NEGADO = "{\"erro\":\"Acesso negado\"}";

    private RespostasSeguranca() {
    }

    static AuthenticationEntryPoint naoAutenticado() {
        return (requisicao, resposta, excecao) ->
                escrever(resposta, HttpStatus.UNAUTHORIZED, CORPO_NAO_AUTENTICADO);
    }

    static AccessDeniedHandler acessoNegado() {
        return (requisicao, resposta, excecao) ->
                escrever(resposta, HttpStatus.FORBIDDEN, CORPO_ACESSO_NEGADO);
    }

    private static void escrever(HttpServletResponse resposta, HttpStatus status, String corpo)
            throws IOException {
        if (resposta.isCommitted()) {
            return;
        }
        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resposta.getWriter().write(corpo);
    }
}
