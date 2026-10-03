package com.thomas.celebrarcatalog.produto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoDto(
        UUID id,
        String nome,
        String descricao,
        BigDecimal preco,
        int quantidade,
        boolean esgotado,
        boolean disponivelNaCesta,
        UUID categoriaId,
        String imagem,
        boolean ativo
) {

    static ProdutoDto from(Produto produto) {
        return new ProdutoDto(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidade(),
                produto.getQuantidade() == 0,
                produto.isDisponivelNaCesta(),
                produto.getCategoria().getId(),
                produto.getImagem(),
                produto.isAtivo()
        );
    }
}
