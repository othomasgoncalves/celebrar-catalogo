package com.thomas.celebrarcatalog.produto;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
class ProdutoController {

    private final ProdutoRepository produtoRepository;

    ProdutoController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping
    List<ProdutoDto> listar(@RequestParam(required = false) UUID categoriaId,
                            @RequestParam(required = false) Boolean disponivelNaCesta) {
        List<Produto> produtos = listarAtivos(categoriaId, Boolean.TRUE.equals(disponivelNaCesta));
        return produtos.stream().map(ProdutoDto::from).toList();
    }

    private List<Produto> listarAtivos(UUID categoriaId, boolean somenteDisponiveisNaCesta) {
        if (somenteDisponiveisNaCesta) {
            return categoriaId == null
                    ? produtoRepository.findAllByAtivoTrueAndDisponivelNaCestaTrue()
                    : produtoRepository.findAllByAtivoTrueAndDisponivelNaCestaTrueAndCategoriaId(categoriaId);
        }
        return categoriaId == null
                ? produtoRepository.findAllByAtivoTrue()
                : produtoRepository.findAllByAtivoTrueAndCategoriaId(categoriaId);
    }
}
