package com.thomas.celebrarcatalog.categoria;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaEntradaDto(

        @NotBlank
        @Size(max = Categoria.NOME_TAMANHO_MAXIMO)
        String nome,

        @NotNull
        @Min(0)
        Integer ordem,

        Boolean ativo
) {

    boolean ativoOuPadrao() {
        return ativo == null || ativo;
    }
}
