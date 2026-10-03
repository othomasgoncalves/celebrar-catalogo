package com.thomas.celebrarcatalog.cesta;

import com.thomas.celebrarcatalog.comum.Campos;
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

    static final int NOME_TAMANHO_MAXIMO = 120;
    static final int DESCRICAO_TAMANHO_MAXIMO = 2000;
    static final int ITENS_TAMANHO_MAXIMO = 2000;

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = NOME_TAMANHO_MAXIMO)
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

    public CestaPronta(String nome,
                       String descricao,
                       BigDecimal preco,
                       String itens,
                       String imagem) {
        this.nome = Campos.obrigatorio(nome, "nome da cesta", NOME_TAMANHO_MAXIMO);
        this.descricao = Campos.opcional(descricao, "descricao da cesta", DESCRICAO_TAMANHO_MAXIMO);
        this.preco = Campos.precoValido(preco);
        this.itens = Campos.obrigatorio(itens, "itens da cesta", ITENS_TAMANHO_MAXIMO);
        this.imagem = Campos.opcional(imagem, "imagem da cesta", 255);
        this.ativo = true;
    }

    public void atualizar(String nome,
                          String descricao,
                          BigDecimal preco,
                          String itens,
                          String imagem,
                          boolean ativo) {
        this.nome = Campos.obrigatorio(nome, "nome da cesta", NOME_TAMANHO_MAXIMO);
        this.descricao = Campos.opcional(descricao, "descricao da cesta", DESCRICAO_TAMANHO_MAXIMO);
        this.preco = Campos.precoValido(preco);
        this.itens = Campos.obrigatorio(itens, "itens da cesta", ITENS_TAMANHO_MAXIMO);
        this.imagem = Campos.opcional(imagem, "imagem da cesta", 255);
        this.ativo = ativo;
    }

    public void desativar() {
        this.ativo = false;
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
