package com.thomas.celebrarcatalog.categoria;

import com.thomas.celebrarcatalog.comum.ConflitoException;
import com.thomas.celebrarcatalog.comum.RecursoNaoEncontradoException;
import com.thomas.celebrarcatalog.produto.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriaService {

    private static final String NOME_REPETIDO = "Ja existe uma categoria com esse nome";

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    CategoriaService(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAllByOrderByOrdemAscNomeAsc();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(UUID id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> naoEncontrada(id));
    }

    @Transactional
    public Categoria criar(CategoriaEntradaDto dados) {
        if (categoriaRepository.existsByNomeIgnoreCase(dados.nome().strip())) {
            throw new ConflitoException(NOME_REPETIDO);
        }
        return categoriaRepository.save(new Categoria(dados.nome(), dados.ordem()));
    }

    @Transactional
    public Categoria atualizar(UUID id, CategoriaEntradaDto dados) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> naoEncontrada(id));

        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(dados.nome().strip(), id)) {
            throw new ConflitoException(NOME_REPETIDO);
        }

        categoria.atualizar(dados.nome(), dados.ordem(), dados.ativoOuPadrao());
        return categoria;
    }

    @Transactional
    public void excluir(UUID id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> naoEncontrada(id));

        if (produtoRepository.existsByCategoriaId(id)) {
            throw new ConflitoException(
                    "A categoria '%s' tem produtos vinculados e nao pode ser excluida. "
                            .formatted(categoria.getNome())
                            + "Mova os produtos para outra categoria ou desative esta categoria "
                            + "(PUT /api/admin/categorias/{id} com \"ativo\": false) para tira-la "
                            + "do catalogo sem perder o historico.");
        }

        categoriaRepository.delete(categoria);
    }

    private static RecursoNaoEncontradoException naoEncontrada(UUID id) {
        return new RecursoNaoEncontradoException("Categoria " + id + " nao encontrada");
    }
}
