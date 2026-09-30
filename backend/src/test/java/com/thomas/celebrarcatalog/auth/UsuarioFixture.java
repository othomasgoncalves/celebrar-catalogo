package com.thomas.celebrarcatalog.auth;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;

/**
 * Constroi {@link Usuario} para os testes. Fica no pacote {@code auth} porque o
 * construtor da entidade e restrito ao pacote; o id, normalmente atribuido pelo
 * Hibernate, e injetado por reflexao.
 */
public final class UsuarioFixture {

    /** Senha em claro correspondente ao hash usado em {@link #admin()}. */
    public static final String SENHA_ADMIN = "CelebrarAdmin!2026";

    /** Mesmo hash BCrypt (custo 12) da migration V4__seed_admin.sql. */
    public static final String HASH_ADMIN = "$2a$12$k.FhHRmT3OkjyoNwAWh8HeF6n5v6nlGYoDEF4Q4KpUZhgcaT8HjC6";

    public static final String EMAIL_ADMIN = "admin@celebrar.local";

    private UsuarioFixture() {
    }

    public static Usuario admin() {
        return comId(new Usuario(EMAIL_ADMIN, HASH_ADMIN, "ROLE_ADMIN", true));
    }

    public static Usuario comRole(String role) {
        return comId(new Usuario(EMAIL_ADMIN, HASH_ADMIN, role, true));
    }

    public static Usuario inativo() {
        return comId(new Usuario(EMAIL_ADMIN, HASH_ADMIN, "ROLE_ADMIN", false));
    }

    public static Usuario bloqueadoAte(Instant instante) {
        Usuario usuario = admin();
        usuario.definirFalhasLogin(TentativasLoginService.MAX_FALHAS);
        usuario.definirBloqueadoAte(instante);
        return usuario;
    }

    public static Usuario comFalhas(int falhas) {
        Usuario usuario = admin();
        usuario.definirFalhasLogin(falhas);
        return usuario;
    }

    private static Usuario comId(Usuario usuario) {
        try {
            Field id = Usuario.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(usuario, UUID.randomUUID());
            return usuario;
        } catch (ReflectiveOperationException excecao) {
            throw new IllegalStateException("Nao foi possivel preparar o Usuario de teste", excecao);
        }
    }
}
