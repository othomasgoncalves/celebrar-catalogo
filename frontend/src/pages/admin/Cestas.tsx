import { useEffect, useMemo, useState } from 'react'
import { atualizarCesta, criarCesta, excluirCesta, listarCestas } from '../../api/admin'
import type { CestaEntrada } from '../../api/admin'
import { CampoFormulario } from '../../components/admin/CampoFormulario'
import { CampoImagem } from '../../components/admin/CampoImagem'
import { ConfirmacaoExclusao } from '../../components/admin/ConfirmacaoExclusao'
import { ModalFormulario } from '../../components/admin/ModalFormulario'
import { PaginaAdmin } from '../../components/admin/PaginaAdmin'
import { TabelaAdmin } from '../../components/admin/TabelaAdmin'
import type { ColunaAdmin } from '../../components/admin/TabelaAdmin'
import { useRecursoAdmin } from '../../hooks/useRecursoAdmin'
import {
  LIMITES,
  juntarErros,
  preco as validarPreco,
  textoObrigatorio,
  textoOpcional,
} from '../../lib/validacao'
import type { Cesta } from '../../types/cesta'
import styles from './Cestas.module.css'

const formatarPreco = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

type FiltroAtivo = 'todos' | 'ativos' | 'inativos'

interface Rascunho {
  nome: string
  descricao: string
  preco: string
  itens: string
  imagem: string | null
  ativo: boolean
}

const RASCUNHO_VAZIO: Rascunho = {
  nome: '',
  descricao: '',
  preco: '',
  itens: '',
  imagem: null,
  ativo: true,
}

function rascunhoDe(cesta: Cesta): Rascunho {
  return {
    nome: cesta.nome,
    descricao: cesta.descricao ?? '',
    preco: cesta.preco.toFixed(2),
    itens: cesta.itens,
    imagem: cesta.imagem,
    ativo: cesta.ativo,
  }
}

export function AdminCestas() {
  const recurso = useRecursoAdmin<Cesta, CestaEntrada>(
    {
      listar: listarCestas,
      criar: criarCesta,
      atualizar: atualizarCesta,
      excluir: excluirCesta,
      idDe: (cesta) => cesta.id,
    },
    {
      criado: 'Cesta criada',
      atualizado: 'Cesta atualizada',
      excluido: 'Cesta removida do catálogo',
    },
  )

  const [filtroAtivo, setFiltroAtivo] = useState<FiltroAtivo>('todos')

  const visiveis = useMemo(() => {
    return recurso.registros.filter((cesta) => {
      if (filtroAtivo === 'ativos' && !cesta.ativo) return false
      if (filtroAtivo === 'inativos' && cesta.ativo) return false
      return true
    })
  }, [recurso.registros, filtroAtivo])

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
      nome: textoObrigatorio(rascunho.nome, LIMITES.nome),
      descricao: textoOpcional(rascunho.descricao, LIMITES.descricao),
      preco: validarPreco(rascunho.preco),
      itens: textoObrigatorio(rascunho.itens, LIMITES.itens),
    })

    await recurso.salvar(
      {
        nome: rascunho.nome.trim(),
        descricao: rascunho.descricao.trim() || null,
        preco: Number(rascunho.preco),
        itens: rascunho.itens.trim(),
        imagem: rascunho.imagem,
        ativo: rascunho.ativo,
      },
      erros,
    )
  }

  const colunas: ColunaAdmin<Cesta>[] = [
    { chave: 'nome', rotulo: 'Nome', conteudo: (cesta) => cesta.nome },
    {
      chave: 'itens',
      rotulo: 'Itens',
      conteudo: (cesta) => <span className={styles.itens}>{cesta.itens}</span>,
    },
    {
      chave: 'preco',
      rotulo: 'Preço',
      estreita: true,
      conteudo: (cesta) => formatarPreco.format(cesta.preco),
    },
  ]

  return (
    <>
      <PaginaAdmin
        titulo="Cestas"
        descricao="Cestas prontas exibidas na vitrine, com itens e preço fechado."
        rotuloNovo="Nova cesta"
        aoCriar={recurso.abrirCriacao}
        carregando={recurso.carregando}
        erro={recurso.erroCarregar}
        aoTentarDeNovo={recurso.recarregar}
        totalVisivel={visiveis.length}
        vazio={
          recurso.registros.length === 0
            ? 'Nenhuma cesta pronta cadastrada ainda.'
            : 'Nenhuma cesta corresponde ao filtro.'
        }
        filtros={
          <label className={styles.filtro}>
            <span>Situação</span>
            <select
              value={filtroAtivo}
              onChange={(evento) => setFiltroAtivo(evento.target.value as FiltroAtivo)}
            >
              <option value="todos">Todas</option>
              <option value="ativos">Só ativas</option>
              <option value="inativos">Só inativas</option>
            </select>
          </label>
        }
      >
        <TabelaAdmin
          registros={visiveis}
          colunas={colunas}
          idDe={(cesta) => cesta.id}
          nomeDe={(cesta) => cesta.nome}
          imagemDe={(cesta) => cesta.imagem}
          ativoDe={(cesta) => cesta.ativo}
          aoEditar={recurso.abrirEdicao}
          aoExcluir={recurso.pedirExclusao}
        />
      </PaginaAdmin>

      {recurso.emEdicao !== null && (
        <ModalFormulario
          titulo={recurso.registroEmEdicao ? 'Editar cesta' : 'Nova cesta'}
          salvando={recurso.salvando}
          erro={recurso.erroFormulario}
          aoFechar={recurso.fecharFormulario}
          aoEnviar={salvar}
        >
          <CampoFormulario rotulo="Nome" erro={recurso.errosCampo.nome}>
            <input
              type="text"
              value={rascunho.nome}
              maxLength={LIMITES.nome}
              onChange={(evento) => alterar('nome', evento.target.value)}
              disabled={recurso.salvando}
            />
          </CampoFormulario>

          <CampoFormulario
            rotulo="Descrição"
            erro={recurso.errosCampo.descricao}
            dica="Opcional, até 2000 caracteres."
          >
            <textarea
              value={rascunho.descricao}
              maxLength={LIMITES.descricao}
              onChange={(evento) => alterar('descricao', evento.target.value)}
              disabled={recurso.salvando}
            />
          </CampoFormulario>

          <CampoFormulario rotulo="Preço (R$)" erro={recurso.errosCampo.preco}>
            <input
              type="number"
              inputMode="decimal"
              step="0.01"
              min="0"
              value={rascunho.preco}
              onChange={(evento) => alterar('preco', evento.target.value)}
              disabled={recurso.salvando}
            />
          </CampoFormulario>

          <CampoFormulario
            rotulo="Itens da cesta"
            erro={recurso.errosCampo.itens}
            dica="O que vai na cesta, como aparece na vitrine. Ex.: 2 brigadeiros gourmet, 1 bolo de pote."
          >
            <textarea
              className={styles.itensCampo}
              value={rascunho.itens}
              maxLength={LIMITES.itens}
              onChange={(evento) => alterar('itens', evento.target.value)}
              disabled={recurso.salvando}
            />
          </CampoFormulario>

          <CampoImagem
            valor={rascunho.imagem}
            aoMudar={(nome) => alterar('imagem', nome)}
            erro={recurso.errosCampo.imagem}
            desabilitado={recurso.salvando}
          />

          <label className={styles.marcador}>
            <input
              type="checkbox"
              checked={rascunho.ativo}
              onChange={(evento) => alterar('ativo', evento.target.checked)}
              disabled={recurso.salvando}
            />
            <span>
              Ativa
              <small>Desmarcada, a cesta sai da vitrine e continua aqui.</small>
            </span>
          </label>
        </ModalFormulario>
      )}

      {recurso.emExclusao && (
        <ConfirmacaoExclusao
          titulo="Remover cesta?"
          descricao="A cesta sai da vitrine, mas continua aqui como inativa — o histórico é preservado e dá para reativá-la depois."
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
