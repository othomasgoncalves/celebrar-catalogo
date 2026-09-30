package com.thomas.celebrarcatalog.comum;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResposta(String erro, Map<String, String> campos) {

    public static ErroResposta de(String erro) {
        return new ErroResposta(erro, null);
    }
}
