package com.thomas.celebrarcatalog.produto;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/produtos")
class ProdutoAdminController {

    private final ProdutoService produtoService;

    ProdutoAdminController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    List<ProdutoDto> listar() {
        return produtoService.listarTodos().stream().map(ProdutoDto::from).toList();
    }

    @GetMapping("/{id}")
    ProdutoDto buscar(@PathVariable UUID id) {
        return ProdutoDto.from(produtoService.buscarPorId(id));
    }

    @PostMapping
    ResponseEntity<ProdutoDto> criar(@Valid @RequestBody ProdutoEntradaDto dados) {
        Produto criado = produtoService.criar(dados);
        return ResponseEntity
                .created(URI.create("/api/admin/produtos/" + criado.getId()))
                .body(ProdutoDto.from(criado));
    }

    @PutMapping("/{id}")
    ProdutoDto atualizar(@PathVariable UUID id, @Valid @RequestBody ProdutoEntradaDto dados) {
        return ProdutoDto.from(produtoService.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable UUID id) {
        produtoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
