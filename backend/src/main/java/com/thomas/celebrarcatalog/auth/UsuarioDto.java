package com.thomas.celebrarcatalog.auth;

import java.util.UUID;

record UsuarioDto(UUID id, String email, String role) {

    static UsuarioDto from(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), usuario.getEmail(), usuario.getRole());
    }

    static UsuarioDto from(UsuarioAutenticado usuario) {
        return new UsuarioDto(usuario.id(), usuario.email(), usuario.role());
    }
}
