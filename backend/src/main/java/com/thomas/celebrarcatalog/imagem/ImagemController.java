package com.thomas.celebrarcatalog.imagem;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.Duration;

@RestController
@RequestMapping("/api/imagens")
class ImagemController {

    private static final CacheControl CACHE = CacheControl
            .maxAge(Duration.ofDays(365))
            .cachePublic()
            .immutable();

    private final ArmazenamentoImagens armazenamento;

    ImagemController(ArmazenamentoImagens armazenamento) {
        this.armazenamento = armazenamento;
    }

    @GetMapping("/{nome}")
    ResponseEntity<Resource> servir(@PathVariable String nome) {
        Path arquivo = armazenamento.localizar(nome).orElse(null);
        if (arquivo == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = NomeImagem.tipoDe(nome)
                .map(TipoImagem::contentType)
                .orElseThrow(() -> new IllegalStateException(
                        "nome validado sem extensao conhecida: " + nome));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .cacheControl(CACHE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(nome).build().toString())
                .body(new FileSystemResource(arquivo));
    }
}
