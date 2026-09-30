package com.thomas.celebrarcatalog.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record LoginRequest(
        @NotBlank(message = "informe o e-mail")
        @Email(message = "e-mail invalido")
        @Size(max = 160, message = "no maximo 160 caracteres")
        String email,

        @NotBlank(message = "informe a senha")
        @Size(max = 72, message = "no maximo 72 caracteres")
        String senha
) {
}
