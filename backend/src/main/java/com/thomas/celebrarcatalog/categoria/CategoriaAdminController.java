package com.thomas.celebrarcatalog.categoria;

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
@RequestMapping("/api/admin/categorias")
class CategoriaAdminController {

    private final CategoriaService categoriaService;

    CategoriaAdminController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    List<CategoriaDto> listar() {
        return categoriaService.listarTodas().stream().map(CategoriaDto::from).toList();
    }

    @GetMapping("/{id}")
    CategoriaDto buscar(@PathVariable UUID id) {
        return CategoriaDto.from(categoriaService.buscarPorId(id));
    }

    @PostMapping
    ResponseEntity<CategoriaDto> criar(@Valid @RequestBody CategoriaEntradaDto dados) {
        Categoria criada = categoriaService.criar(dados);
        return ResponseEntity
                .created(URI.create("/api/admin/categorias/" + criada.getId()))
                .body(CategoriaDto.from(criada));
    }

    @PutMapping("/{id}")
    CategoriaDto atualizar(@PathVariable UUID id, @Valid @RequestBody CategoriaEntradaDto dados) {
        return CategoriaDto.from(categoriaService.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable UUID id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
