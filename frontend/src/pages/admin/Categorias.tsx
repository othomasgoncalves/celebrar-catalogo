import { useEffect, useState } from 'react'
import {
  atualizarCategoria,
  criarCategoria,
  excluirCategoria,
  listarCategorias,
} from '../../api/admin'
import type { CategoriaEntrada } from '../../api/admin'
import { CampoFormulario } from '../../components/admin/CampoFormulario'
import { ConfirmacaoExclusao } from '../../components/admin/ConfirmacaoExclusao'
import { ModalFormulario } from '../../components/admin/ModalFormulario'
import { PaginaAdmin } from '../../components/admin/PaginaAdmin'
import { TabelaAdmin } from '../../components/admin/TabelaAdmin'
import type { ColunaAdmin } from '../../components/admin/TabelaAdmin'
import { useRecursoAdmin } from '../../hooks/useRecursoAdmin'
import { LIMITES, inteiroNaoNegativo, juntarErros, textoObrigatorio } from '../../lib/validacao'
import type { Categoria } from '../../types/categoria'
import styles from './Categorias.module.css'

interface Rascunho {
  nome: string
  ordem: string
  ativo: boolean
}

const RASCUNHO_VAZIO: Rascunho = { nome: '', ordem: '0', ativo: true }

function rascunhoDe(categoria: Categoria): Rascunho {
  return {
    nome: categoria.nome,
    ordem: String(categoria.ordem),
    ativo: categoria.ativo,
  }
}

export function AdminCategorias() {
  const recurso = useRecursoAdmin<Categoria, CategoriaEntrada>(
    {
      listar: listarCategorias,
      criar: criarCategoria,
      atualizar: atualizarCategoria,
      excluir: excluirCategoria,
      idDe: (categoria) => categoria.id,
    },
    {
      criado: 'Categoria criada',
      atualizado: 'Categoria atualizada',
      excluido: 'Categoria excluída',
    },
  )

  const [rascunho, setRascunho] = useState<Rascunho>(RASCUNHO_VAZIO)

  useEffect(() => {
    if (recurso.emEdicao === null) return
    setRascunho(recurso.registroEmEdicao ? rascunhoDe(recurso.registroEmEdicao) : RASCUNHO_VAZIO)
  }, [recurso.emEdicao, recurso.registroEmEdicao])

  function alterar<C extends keyof Rascunho>(campo: C, valor: Rascunho[C]) {
    setRascunho((atual) => ({ ...atual, [campo]: valor }))
  }

  async function salvar() {
    const erros = juntarErros({
      nome: textoObrigatorio(rascunho.nome, LIMITES.nomeCategoria),
      ordem: inteiroNaoNegativo(rascunho.ordem),
    })

    await recurso.salvar(
      {
        nome: rascunho.nome.trim(),
        ordem: Number(rascunho.ordem),
        ativo: rascunho.ativo,
      },
      erros,
    )
  }

  const colunas: ColunaAdmin<Categoria>[] = [
    { chave: 'nome', rotulo: 'Nome', conteudo: (categoria) => categoria.nome },
    {
      chave: 'ordem',
      rotulo: 'Ordem',
      estreita: true,
      conteudo: (categoria) => categoria.ordem,
    },
  ]

  return (
    <>
      <PaginaAdmin
        titulo="Categorias"
        descricao="Categorias do catálogo e a ordem em que aparecem no filtro."
        rotuloNovo="Nova categoria"
        aoCriar={recurso.abrirCriacao}
        carregando={recurso.carregando}
        erro={recurso.erroCarregar}
        aoTentarDeNovo={recurso.recarregar}
        totalVisivel={recurso.registros.length}
        vazio="Nenhuma categoria cadastrada ainda."
      >
        <TabelaAdmin
          registros={recurso.registros}
          colunas={colunas}
          idDe={(categoria) => categoria.id}
          nomeDe={(categoria) => categoria.nome}
          ativoDe={(categoria) => categoria.ativo}
          aoEditar={recurso.abrirEdicao}
          aoExcluir={recurso.pedirExclusao}
        />
      </PaginaAdmin>

      {recurso.emEdicao !== null && (
        <ModalFormulario
          titulo={recurso.registroEmEdicao ? 'Editar categoria' : 'Nova categoria'}
          salvando={recurso.salvando}
          erro={recurso.erroFormulario}
          aoFechar={recurso.fecharFormulario}
          aoEnviar={salvar}
        >
          <CampoFormulario
            rotulo="Nome"
            erro={recurso.errosCampo.nome}
            dica="Até 60 caracteres. Não pode repetir o nome de outra categoria."
          >
            <input
              type="text"
              value={rascunho.nome}
              maxLength={LIMITES.nomeCategoria}
              onChange={(evento) => alterar('nome', evento.target.value)}
              disabled={recurso.salvando}
            />
          </CampoFormulario>

          <CampoFormulario
            rotulo="Ordem"
            erro={recurso.errosCampo.ordem}
            dica="Menor aparece primeiro no filtro do catálogo."
          >
            <input
              type="number"
              inputMode="numeric"
              step="1"
              min="0"
              value={rascunho.ordem}
              onChange={(evento) => alterar('ordem', evento.target.value)}
              disabled={recurso.salvando}
            />
          </CampoFormulario>

          <label className={styles.marcador}>
            <input
              type="checkbox"
              checked={rascunho.ativo}
              onChange={(evento) => alterar('ativo', evento.target.checked)}
              disabled={recurso.salvando}
            />
            <span>
              Ativa
              <small>
                Desmarcada, a categoria sai do filtro público. É a alternativa à exclusão
                quando ela já tem produtos.
              </small>
            </span>
          </label>
        </ModalFormulario>
      )}

      {recurso.emExclusao && (
        <ConfirmacaoExclusao
          titulo="Excluir categoria?"
          descricao="Esta exclusão é definitiva. Só funciona se nenhum produto estiver na categoria — se houver, desative-a em vez de excluir."
          nome={recurso.emExclusao.nome}
          excluindo={recurso.excluindo}
          erro={recurso.erroExclusao}
          aoConfirmar={recurso.confirmarExclusao}
          aoCancelar={recurso.cancelarExclusao}
        />
      )}
    </>
  )
}
