package com.thomas.celebrarcatalog.cesta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cesta_pronta")
public class CestaPronta {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String itens;

    @Column(length = 255)
    private String imagem;

    @Column(nullable = false)
    private boolean ativo;

    protected CestaPronta() {
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public String getItens() {
        return itens;
    }

    public String getImagem() {
        return imagem;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
