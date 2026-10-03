package com.thomas.celebrarcatalog.produto;

import com.thomas.celebrarcatalog.categoria.Categoria;
import com.thomas.celebrarcatalog.comum.Campos;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "produto")
public class Produto {

    static final int NOME_TAMANHO_MAXIMO = 120;
    static final int DESCRICAO_TAMANHO_MAXIMO = 2000;

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = NOME_TAMANHO_MAXIMO)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private int quantidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(length = 255)
    private String imagem;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "disponivel_na_cesta", nullable = false)
    private boolean disponivelNaCesta;

    protected Produto() {
    }

    public Produto(String nome,
                   String descricao,
                   BigDecimal preco,
                   int quantidade,
                   Categoria categoria,
                   String imagem,
                   boolean disponivelNaCesta) {
        this.nome = Campos.obrigatorio(nome, "nome do produto", NOME_TAMANHO_MAXIMO);
        this.descricao = Campos.opcional(descricao, "descricao do produto", DESCRICAO_TAMANHO_MAXIMO);
        this.preco = Campos.precoValido(preco);
        this.quantidade = Campos.quantidadeValida(quantidade);
        this.categoria = categoriaValida(categoria);
        this.imagem = Campos.opcional(imagem, "imagem do produto", 255);
        this.disponivelNaCesta = disponivelNaCesta;
        this.ativo = true;
    }

    public void atualizar(String nome,
                          String descricao,
                          BigDecimal preco,
                          int quantidade,
                          Categoria categoria,
                          String imagem,
                          boolean disponivelNaCesta,
                          boolean ativo) {
        this.nome = Campos.obrigatorio(nome, "nome do produto", NOME_TAMANHO_MAXIMO);
        this.descricao = Campos.opcional(descricao, "descricao do produto", DESCRICAO_TAMANHO_MAXIMO);
        this.preco = Campos.precoValido(preco);
        this.quantidade = Campos.quantidadeValida(quantidade);
        this.categoria = categoriaValida(categoria);
        this.imagem = Campos.opcional(imagem, "imagem do produto", 255);
        this.disponivelNaCesta = disponivelNaCesta;
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

    public int getQuantidade() {
        return quantidade;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getImagem() {
        return imagem;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean isDisponivelNaCesta() {
        return disponivelNaCesta;
    }

    private static Categoria categoriaValida(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("categoria do produto e obrigatoria");
        }
        return categoria;
    }
}
