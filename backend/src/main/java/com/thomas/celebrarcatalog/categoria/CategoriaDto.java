package com.thomas.celebrarcatalog.categoria;

import java.util.UUID;

public record CategoriaDto(UUID id, String nome, int ordem, boolean ativo) {

    static CategoriaDto from(Categoria categoria) {
        return new CategoriaDto(
                categoria.getId(),
                categoria.getNome(),
                categoria.getOrdem(),
                categoria.isAtivo()
        );
    }
}
