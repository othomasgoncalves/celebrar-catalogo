package com.thomas.celebrarcatalog.auth;

import java.util.UUID;

public record UsuarioAutenticado(UUID id, String email, String role) {
}
