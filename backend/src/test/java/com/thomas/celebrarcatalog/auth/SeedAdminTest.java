package com.thomas.celebrarcatalog.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Amarra a senha documentada no README ao hash que esta na migration. Se alguem trocar
 * um dos dois sem trocar o outro, este teste avisa antes de o admin ficar sem acesso.
 */
class SeedAdminTest {

    private static final Path MIGRATION =
            Path.of("src/main/resources/db/migration/V4__seed_admin.sql");

    @Test
    void o_hash_da_migration_corresponde_a_senha_documentada() {
        assertThat(new BCryptPasswordEncoder(12)
                .matches(UsuarioFixture.SENHA_ADMIN, UsuarioFixture.HASH_ADMIN)).isTrue();
    }

    @Test
    void a_migration_usa_exatamente_esse_hash_com_role_admin() throws IOException {
        String sql = Files.readString(MIGRATION, StandardCharsets.UTF_8);

        assertThat(sql).contains(UsuarioFixture.HASH_ADMIN);
        assertThat(sql).contains(UsuarioFixture.EMAIL_ADMIN);
        assertThat(sql).contains("ROLE_ADMIN");
    }

    @Test
    void o_hash_usa_custo_12() {
        // Formato: $2a$<custo>$<salt+hash>
        assertThat(UsuarioFixture.HASH_ADMIN).startsWith("$2a$12$");
        assertThat(UsuarioFixture.HASH_ADMIN).hasSize(60);
    }

    @Test
    void a_senha_documentada_nao_e_aceita_por_outro_hash() {
        assertThat(new BCryptPasswordEncoder(12)
                .matches("outra-senha", UsuarioFixture.HASH_ADMIN)).isFalse();
    }
}
