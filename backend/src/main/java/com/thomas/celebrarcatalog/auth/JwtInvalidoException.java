package com.thomas.celebrarcatalog.auth;

public class JwtInvalidoException extends RuntimeException {

    JwtInvalidoException(String motivo) {
        super(motivo);
    }

    JwtInvalidoException(String motivo, Throwable causa) {
        super(motivo, causa);
    }
}
