package com.thomas.celebrarcatalog.cesta;

import com.thomas.celebrarcatalog.comum.RecursoNaoEncontradoException;
import com.thomas.celebrarcatalog.imagem.ArmazenamentoImagens;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CestaProntaService {

    private final CestaProntaRepository cestaProntaRepository;
    private final ArmazenamentoImagens armazenamento;

    CestaProntaService(CestaProntaRepository cestaProntaRepository,
                       ArmazenamentoImagens armazenamento) {
        this.cestaProntaRepository = cestaProntaRepository;
        this.armazenamento = armazenamento;
    }

    @Transactional(readOnly = true)
    public List<CestaPronta> listarTodas() {
        return cestaProntaRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public CestaPronta buscarPorId(UUID id) {
        return cestaProntaRepository.findById(id).orElseThrow(() -> naoEncontrada(id));
    }

    @Transactional
    public CestaPronta criar(CestaProntaEntradaDto dados) {
        return cestaProntaRepository.save(new CestaPronta(
                dados.nome(),
                dados.descricao(),
                dados.preco(),
                dados.itens(),
                dados.imagem()));
    }

    @Transactional
    public CestaPronta atualizar(UUID id, CestaProntaEntradaDto dados) {
        CestaPronta cesta = cestaProntaRepository.findById(id)
                .orElseThrow(() -> naoEncontrada(id));

        String imagemAnterior = cesta.getImagem();

        cesta.atualizar(
                dados.nome(),
                dados.descricao(),
                dados.preco(),
                dados.itens(),
                dados.imagem(),
                dados.ativoOuPadrao());

        apagarImagemSubstituida(imagemAnterior, cesta.getImagem());
        return cesta;
    }

    @Transactional
    public void excluir(UUID id) {
        cestaProntaRepository.findById(id).orElseThrow(() -> naoEncontrada(id)).desativar();
    }

    private void apagarImagemSubstituida(String anterior, String atual) {
        if (anterior != null && !Objects.equals(anterior, atual)) {
            armazenamento.apagar(anterior);
        }
    }

    private static RecursoNaoEncontradoException naoEncontrada(UUID id) {
        return new RecursoNaoEncontradoException("Cesta " + id + " nao encontrada");
    }
}
