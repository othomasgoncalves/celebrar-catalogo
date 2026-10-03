package com.thomas.celebrarcatalog.cesta;

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
@RequestMapping("/api/admin/cestas")
class CestaProntaAdminController {

    private final CestaProntaService cestaProntaService;

    CestaProntaAdminController(CestaProntaService cestaProntaService) {
        this.cestaProntaService = cestaProntaService;
    }

    @GetMapping
    List<CestaProntaDto> listar() {
        return cestaProntaService.listarTodas().stream().map(CestaProntaDto::from).toList();
    }

    @GetMapping("/{id}")
    CestaProntaDto buscar(@PathVariable UUID id) {
        return CestaProntaDto.from(cestaProntaService.buscarPorId(id));
    }

    @PostMapping
    ResponseEntity<CestaProntaDto> criar(@Valid @RequestBody CestaProntaEntradaDto dados) {
        CestaPronta criada = cestaProntaService.criar(dados);
        return ResponseEntity
                .created(URI.create("/api/admin/cestas/" + criada.getId()))
                .body(CestaProntaDto.from(criada));
    }

    @PutMapping("/{id}")
    CestaProntaDto atualizar(@PathVariable UUID id,
                             @Valid @RequestBody CestaProntaEntradaDto dados) {
        return CestaProntaDto.from(cestaProntaService.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> excluir(@PathVariable UUID id) {
        cestaProntaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
