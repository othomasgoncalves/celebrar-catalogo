package com.thomas.celebrarcatalog.imagem;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/imagens")
class ImagemAdminController {

    private final ArmazenamentoImagens armazenamento;

    ImagemAdminController(ArmazenamentoImagens armazenamento) {
        this.armazenamento = armazenamento;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ImagemCriadaDto> enviar(@RequestParam("arquivo") MultipartFile arquivo) {
        String nome = armazenamento.salvar(arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ImagemCriadaDto(nome));
    }
}
