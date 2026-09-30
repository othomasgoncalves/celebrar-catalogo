package com.thomas.celebrarcatalog.cesta;

import java.math.BigDecimal;
import java.util.UUID;

public record CestaProntaDto(
        UUID id,
        String nome,
        String descricao,
        BigDecimal preco,
        String itens,
        String imagem
) {

    static CestaProntaDto from(CestaPronta cesta) {
        return new CestaProntaDto(
                cesta.getId(),
                cesta.getNome(),
                cesta.getDescricao(),
                cesta.getPreco(),
                cesta.getItens(),
                cesta.getImagem()
        );
    }
}
