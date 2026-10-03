package com.thomas.celebrarcatalog.produto;

import com.thomas.celebrarcatalog.imagem.NomeImagem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoEntradaDto(

        @NotBlank
        @Size(max = Produto.NOME_TAMANHO_MAXIMO)
        String nome,

        @Size(max = Produto.DESCRICAO_TAMANHO_MAXIMO)
        String descricao,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal preco,

        @NotNull
        @Min(0)
        Integer quantidade,

        @NotNull
        UUID categoriaId,

        @Pattern(regexp = NomeImagem.REGEX, message = "deve ser um nome gerado pelo upload de imagens")
        String imagem,

        Boolean disponivelNaCesta,

        Boolean ativo
) {

    boolean disponivelNaCestaOuPadrao() {
        return disponivelNaCesta != null && disponivelNaCesta;
    }

    boolean ativoOuPadrao() {
        return ativo == null || ativo;
    }
}
