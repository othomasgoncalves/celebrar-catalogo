package com.thomas.celebrarcatalog.cesta;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cestas")
class CestaProntaController {

    private final CestaProntaRepository cestaProntaRepository;

    CestaProntaController(CestaProntaRepository cestaProntaRepository) {
        this.cestaProntaRepository = cestaProntaRepository;
    }

    @GetMapping
    List<CestaProntaDto> listar() {
        return cestaProntaRepository.findAllByAtivoTrue()
                .stream()
                .map(CestaProntaDto::from)
                .toList();
    }
}
