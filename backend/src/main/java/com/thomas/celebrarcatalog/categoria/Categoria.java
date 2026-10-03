package com.thomas.celebrarcatalog.categoria;

import com.thomas.celebrarcatalog.comum.Campos;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "categoria")
public class Categoria {

    static final int NOME_TAMANHO_MAXIMO = 60;

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = NOME_TAMANHO_MAXIMO)
    private String nome;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false)
    private boolean ativo;

    protected Categoria() {
    }

    public Categoria(String nome, int ordem) {
        this.nome = Campos.obrigatorio(nome, "nome da categoria", NOME_TAMANHO_MAXIMO);
        this.ordem = Campos.ordemValida(ordem);
        this.ativo = true;
    }

    public void atualizar(String nome, int ordem, boolean ativo) {
        this.nome = Campos.obrigatorio(nome, "nome da categoria", NOME_TAMANHO_MAXIMO);
        this.ordem = Campos.ordemValida(ordem);
        this.ativo = ativo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getOrdem() {
        return ordem;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
