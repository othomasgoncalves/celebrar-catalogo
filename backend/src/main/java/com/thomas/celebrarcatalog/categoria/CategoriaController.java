package com.thomas.celebrarcatalog.categoria;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    List<CategoriaDto> listar() {
        return categoriaRepository.findAllByOrderByOrdemAscNomeAsc()
                .stream()
                .map(CategoriaDto::from)
                .toList();
    }
}
