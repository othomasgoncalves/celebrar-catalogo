package com.thomas.celebrarcatalog.comum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException excecao) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : excecao.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), mensagemDe(erro));
        }
        return ResponseEntity.badRequest().body(new ErroResposta("Dados invalidos", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResposta> tratarCorpoIlegivel(HttpMessageNotReadableException excecao) {
        log.debug("Corpo da requisicao ilegivel: {}", excecao.getMessage());
        return ResponseEntity.badRequest().body(ErroResposta.de("Corpo da requisicao invalido"));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroResposta.de(excecao.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResposta> tratarInesperado(Exception excecao) {
        if (excecao instanceof ErrorResponse erroComStatus) {
            log.debug("Requisicao rejeitada com status {}: {}",
                    erroComStatus.getStatusCode(), excecao.getMessage());
            return ResponseEntity.status(erroComStatus.getStatusCode())
                    .body(ErroResposta.de("Requisicao nao atendida"));
        }
        log.error("Erro inesperado ao processar a requisicao", excecao);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResposta.de("Erro inesperado"));
    }

    private static String mensagemDe(FieldError erro) {
        return erro.getDefaultMessage() == null ? "valor invalido" : erro.getDefaultMessage();
    }
}
