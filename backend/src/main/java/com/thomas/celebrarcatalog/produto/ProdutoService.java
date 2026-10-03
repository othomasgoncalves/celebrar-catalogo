package com.thomas.celebrarcatalog.produto;

import com.thomas.celebrarcatalog.categoria.Categoria;
import com.thomas.celebrarcatalog.categoria.CategoriaRepository;
import com.thomas.celebrarcatalog.comum.RecursoNaoEncontradoException;
import com.thomas.celebrarcatalog.imagem.ArmazenamentoImagens;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ArmazenamentoImagens armazenamento;

    ProdutoService(ProdutoRepository produtoRepository,
                   CategoriaRepository categoriaRepository,
                   ArmazenamentoImagens armazenamento) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.armazenamento = armazenamento;
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(UUID id) {
        return produtoRepository.findById(id).orElseThrow(() -> naoEncontrado(id));
    }

    @Transactional
    public Produto criar(ProdutoEntradaDto dados) {
        return produtoRepository.save(new Produto(
                dados.nome(),
                dados.descricao(),
                dados.preco(),
                dados.quantidade(),
                categoria(dados.categoriaId()),
                dados.imagem(),
                dados.disponivelNaCestaOuPadrao()));
    }

    @Transactional
    public Produto atualizar(UUID id, ProdutoEntradaDto dados) {
        Produto produto = produtoRepository.findById(id).orElseThrow(() -> naoEncontrado(id));

        String imagemAnterior = produto.getImagem();

        produto.atualizar(
                dados.nome(),
                dados.descricao(),
                dados.preco(),
                dados.quantidade(),
                categoria(dados.categoriaId()),
                dados.imagem(),
                dados.disponivelNaCestaOuPadrao(),
                dados.ativoOuPadrao());

        apagarImagemSubstituida(imagemAnterior, produto.getImagem());
        return produto;
    }

    @Transactional
    public void excluir(UUID id) {
        produtoRepository.findById(id).orElseThrow(() -> naoEncontrado(id)).desativar();
    }

    private Categoria categoria(UUID categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria " + categoriaId + " nao encontrada"));
    }
    
    private void apagarImagemSubstituida(String anterior, String atual) {
        if (anterior != null && !Objects.equals(anterior, atual)) {
            armazenamento.apagar(anterior);
        }
    }

    private static RecursoNaoEncontradoException naoEncontrado(UUID id) {
        return new RecursoNaoEncontradoException("Produto " + id + " nao encontrado");
    }
}
