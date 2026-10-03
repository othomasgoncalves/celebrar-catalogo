package com.thomas.celebrarcatalog.imagem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Component
public class ArmazenamentoImagens {

    private static final Logger log = LoggerFactory.getLogger(ArmazenamentoImagens.class);

    private final Path base;

    ArmazenamentoImagens(ImagemProperties propriedades) {
        this.base = propriedades.diretorio();
        try {
            Files.createDirectories(base);
        } catch (IOException erro) {
            throw new IllegalStateException(
                    "Nao foi possivel criar o diretorio de imagens (app.imagens.diretorio): " + base,
                    erro);
        }
    }

    public String salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ImagemInvalidaException("Envie um arquivo de imagem no campo 'arquivo'");
        }
        if (!TipoImagem.contentTypeAceito(arquivo.getContentType())) {
            throw new ImagemInvalidaException(
                    "Formato nao aceito. Envie uma imagem JPEG, PNG ou WebP");
        }

        TipoImagem tipo = detectarFormato(arquivo);
        String nome = NomeImagem.gerar(tipo);
        Path destino = resolverDentroDaBase(nome);

        try (InputStream conteudo = arquivo.getInputStream()) {
            Files.copy(conteudo, destino);
        } catch (IOException erro) {
            throw new UncheckedIOException("Falha ao gravar a imagem " + nome, erro);
        }
        return nome;
    }

    public void apagar(String nome) {
        if (nome == null || nome.isBlank()) {
            return;
        }
        if (!NomeImagem.valido(nome)) {
            log.warn("Ignorando remocao de imagem com nome fora do padrao");
            return;
        }
        try {
            Files.deleteIfExists(resolverDentroDaBase(nome));
        } catch (IOException | ImagemInvalidaException erro) {
            log.warn("Nao foi possivel apagar a imagem {}", nome, erro);
        }
    }

    public Optional<Path> localizar(String nome) {
        if (!NomeImagem.valido(nome)) {
            return Optional.empty();
        }
        Path arquivo;
        try {
            arquivo = resolverDentroDaBase(nome);
        } catch (ImagemInvalidaException erro) {
            return Optional.empty();
        }
        return Files.isRegularFile(arquivo) ? Optional.of(arquivo) : Optional.empty();
    }

    private Path resolverDentroDaBase(String nome) {
        Path resolvido = base.resolve(nome).normalize();
        if (!resolvido.startsWith(base) || resolvido.equals(base)) {
            throw new ImagemInvalidaException("Nome de imagem invalido");
        }
        return resolvido;
    }

    private static TipoImagem detectarFormato(MultipartFile arquivo) {
        byte[] prefixo = new byte[TipoImagem.BYTES_NECESSARIOS];
        int lidos;
        try (InputStream conteudo = arquivo.getInputStream()) {
            lidos = conteudo.readNBytes(prefixo, 0, prefixo.length);
        } catch (IOException erro) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", erro);
        }
        byte[] inicio = lidos == prefixo.length ? prefixo : Arrays.copyOf(prefixo, lidos);

        return TipoImagem.detectar(inicio).orElseThrow(() -> new ImagemInvalidaException(
                "O conteudo do arquivo nao e uma imagem JPEG, PNG ou WebP"));
    }
}
