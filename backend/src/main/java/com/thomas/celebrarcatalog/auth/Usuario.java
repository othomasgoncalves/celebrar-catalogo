package com.thomas.celebrarcatalog.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 72)
    private String senhaHash;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "falhas_login", nullable = false)
    private int falhasLogin;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;

    @JdbcTypeCode(SqlTypes.TIMESTAMP_UTC)
    @Column(name = "criado_em", nullable = false, insertable = false, updatable = false)
    private Instant criadoEm;

    protected Usuario() {
    }

    Usuario(String email, String senhaHash, String role, boolean ativo) {
        this.email = email;
        this.senhaHash = senhaHash;
        this.role = role;
        this.ativo = ativo;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public String getRole() {
        return role;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getFalhasLogin() {
        return falhasLogin;
    }

    public Instant getBloqueadoAte() {
        return bloqueadoAte;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    void definirFalhasLogin(int falhasLogin) {
        this.falhasLogin = falhasLogin;
    }

    void definirBloqueadoAte(Instant bloqueadoAte) {
        this.bloqueadoAte = bloqueadoAte;
    }
}
