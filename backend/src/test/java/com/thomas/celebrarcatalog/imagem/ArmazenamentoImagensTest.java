package com.thomas.celebrarcatalog.imagem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O upload de imagem e o unico ponto da API que grava um arquivo com nome derivado de
 * dados da requisicao, entao os testes aqui sao quase todos sobre entrada hostil:
 * content-type mentiroso, conteudo que nao e imagem e tentativas de path traversal.
 */
class ArmazenamentoImagensTest {

    private static final byte[] PNG = HexFormat.of().parseHex("89504e470d0a1a0a0000000d49484452");
    private static final byte[] JPEG = HexFormat.of().parseHex("ffd8ffe000104a46494600010100");
    private static final byte[] WEBP = "RIFF\u0000\u0000\u0000\u0000WEBPVP8 ".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);

    @TempDir
    private Path diretorio;

    private ArmazenamentoImagens armazenamento;

    @BeforeEach
    void preparar() {
        armazenamento = new ArmazenamentoImagens(new ImagemProperties(diretorio));
    }

    // --- nome gerado ---

    @Test
    void grava_o_arquivo_com_nome_uuid_e_extensao_do_formato_detectado() {
        String nome = armazenamento.salvar(arquivo("foto.png", "image/png", PNG));

        assertThat(nome).matches("^" + NomeImagem.REGEX + "$");
        assertThat(nome).endsWith(".png");
        assertThat(diretorio.resolve(nome)).isRegularFile().hasBinaryContent(PNG);
    }

    @Test
    void descarta_por_completo_o_nome_original_enviado_pelo_cliente() {
        String nome = armazenamento.salvar(
                arquivo("minha-foto-de-bolo.png", "image/png", PNG));

        // Nenhum pedaco do nome original sobrevive: o nome e sorteado, nao derivado.
        assertThat(nome).doesNotContain("minha").doesNotContain("bolo").doesNotContain("foto");
    }

    @Test
    void dois_uploads_do_mesmo_arquivo_geram_nomes_diferentes() {
        String primeiro = armazenamento.salvar(arquivo("a.png", "image/png", PNG));
        String segundo = armazenamento.salvar(arquivo("a.png", "image/png", PNG));

        assertThat(primeiro).isNotEqualTo(segundo);
        assertThat(diretorio).isDirectoryContaining(caminho -> caminho.getFileName().toString().equals(primeiro))
                .isDirectoryContaining(caminho -> caminho.getFileName().toString().equals(segundo));
    }

    /**
     * O nome original e o vetor classico de path traversal. Como ele e descartado antes
     * de qualquer uso, nem um nome montado para escapar do diretorio tem efeito: o
     * arquivo cai na base, com nome novo, e nada e escrito fora dela.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "../../../../etc/passwd.png",
            "..\\..\\windows\\system32\\evil.png",
            "/etc/cron.d/backdoor.png",
            "C:\\Windows\\Temp\\evil.png",
            "foto.png\u0000.jsp",
            ".././.././foto.png"
    })
    void nome_original_com_path_traversal_nao_escapa_do_diretorio(String nomeOriginal) throws Exception {
        String nome = armazenamento.salvar(arquivo(nomeOriginal, "image/png", PNG));

        assertThat(diretorio.resolve(nome)).isRegularFile();
        // Um unico arquivo, e dentro da base: nada vazou para fora.
        try (var conteudo = Files.walk(diretorio)) {
            assertThat(conteudo.filter(Files::isRegularFile)).containsExactly(diretorio.resolve(nome));
        }
    }

    // --- formatos aceitos ---

    @Test
    void aceita_jpeg_png_e_webp_pelo_conteudo() {
        assertThat(armazenamento.salvar(arquivo("a.jpg", "image/jpeg", JPEG))).endsWith(".jpg");
        assertThat(armazenamento.salvar(arquivo("a.png", "image/png", PNG))).endsWith(".png");
        assertThat(armazenamento.salvar(arquivo("a.webp", "image/webp", WEBP))).endsWith(".webp");
    }

    @Test
    void recusa_content_type_fora_da_whitelist() {
        assertThatThrownBy(() -> armazenamento.salvar(arquivo("a.svg", "image/svg+xml", PNG)))
                .isInstanceOf(ImagemInvalidaException.class)
                .hasMessageContaining("JPEG, PNG ou WebP");
    }

    @Test
    void recusa_arquivo_sem_content_type() {
        assertThatThrownBy(() -> armazenamento.salvar(arquivo("a.png", null, PNG)))
                .isInstanceOf(ImagemInvalidaException.class);
    }

    /**
     * O coracao da validacao: o cliente controla o content-type e a extensao, mas nao os
     * magic bytes. Um script com header de imagem e recusado.
     */
    @Test
    void recusa_conteudo_que_nao_e_imagem_mesmo_com_content_type_de_imagem() {
        byte[] script = "<?php system($_GET['c']); ?>".getBytes();

        assertThatThrownBy(() -> armazenamento.salvar(arquivo("inocente.png", "image/png", script)))
                .isInstanceOf(ImagemInvalidaException.class)
                .hasMessageContaining("nao e uma imagem");

        assertThat(diretorio).isEmptyDirectory();
    }

    @Test
    void recusa_png_declarado_que_na_verdade_e_outro_formato_aceito_nao_grava_extensao_mentirosa() {
        // Content-type diz PNG, conteudo e JPEG: quem manda e o conteudo.
        String nome = armazenamento.salvar(arquivo("a.png", "image/png", JPEG));

        assertThat(nome).endsWith(".jpg");
    }

    @Test
    void recusa_arquivo_curto_demais_para_ter_assinatura() {
        assertThatThrownBy(() -> armazenamento.salvar(arquivo("a.png", "image/png", new byte[]{(byte) 0x89, 0x50})))
                .isInstanceOf(ImagemInvalidaException.class);
    }

    @Test
    void recusa_arquivo_vazio() {
        assertThatThrownBy(() -> armazenamento.salvar(arquivo("a.png", "image/png", new byte[0])))
                .isInstanceOf(ImagemInvalidaException.class);
    }

    // --- leitura ---

    @Test
    void localiza_o_arquivo_gravado() {
        String nome = armazenamento.salvar(arquivo("a.png", "image/png", PNG));

        assertThat(armazenamento.localizar(nome)).contains(diretorio.resolve(nome));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../../../etc/passwd",
            "..%2f..%2fetc%2fpasswd",
            "foto.png",
            "NAO-E-UUID.png",
            "ab6a2f1e-0000-0000-0000-000000000000.php",
            "ab6a2f1e-0000-0000-0000-000000000000.png.php",
            "AB6A2F1E-0000-0000-0000-000000000000.png",
            "ab6a2f1e-0000-0000-0000-000000000000.png/../../x"
    })
    void nao_localiza_nome_fora_do_padrao_uuid_mais_extensao(String nome) {
        assertThat(armazenamento.localizar(nome)).isEmpty();
    }

    @Test
    void nao_localiza_arquivo_inexistente_com_nome_valido() {
        assertThat(armazenamento.localizar("ab6a2f1e-1111-4111-8111-111111111111.png")).isEmpty();
    }

    // --- remocao ---

    @Test
    void apaga_o_arquivo_gravado() {
        String nome = armazenamento.salvar(arquivo("a.png", "image/png", PNG));

        armazenamento.apagar(nome);

        assertThat(diretorio.resolve(nome)).doesNotExist();
    }

    @Test
    void apagar_e_tolerante_a_nome_invalido_nulo_e_arquivo_inexistente() {
        // Melhor esforco: nenhum desses casos pode derrubar a escrita que o banco aceitou.
        armazenamento.apagar(null);
        armazenamento.apagar("");
        armazenamento.apagar("../../../etc/passwd");
        armazenamento.apagar("ab6a2f1e-1111-4111-8111-111111111111.png");
    }

    @Test
    void apagar_nome_fora_do_padrao_nao_remove_arquivo_de_fora_da_base() throws Exception {
        Path vizinho = diretorio.resolveSibling("nao-me-apague.txt");
        Files.writeString(vizinho, "importante");

        armazenamento.apagar("../" + vizinho.getFileName());

        assertThat(vizinho).exists();
    }

    private static MockMultipartFile arquivo(String nomeOriginal, String contentType, byte[] conteudo) {
        return new MockMultipartFile("arquivo", nomeOriginal, contentType, conteudo);
    }
}
