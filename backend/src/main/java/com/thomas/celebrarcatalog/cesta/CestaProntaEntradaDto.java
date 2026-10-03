package com.thomas.celebrarcatalog.cesta;

import com.thomas.celebrarcatalog.imagem.NomeImagem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CestaProntaEntradaDto(

        @NotBlank
        @Size(max = CestaPronta.NOME_TAMANHO_MAXIMO)
        String nome,

        @Size(max = CestaPronta.DESCRICAO_TAMANHO_MAXIMO)
        String descricao,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal preco,

        @NotBlank
        @Size(max = CestaPronta.ITENS_TAMANHO_MAXIMO)
        String itens,

        @Pattern(regexp = NomeImagem.REGEX, message = "deve ser um nome gerado pelo upload de imagens")
        String imagem,

        Boolean ativo
) {

    boolean ativoOuPadrao() {
        return ativo == null || ativo;
    }
}
