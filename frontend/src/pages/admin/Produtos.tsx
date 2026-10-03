import { useEffect, useMemo, useState } from 'react'
import {
  atualizarProduto,
  criarProduto,
  excluirProduto,
  listarCategorias,
  listarProdutos,
} from '../../api/admin'
import type { ProdutoEntrada } from '../../api/admin'
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
  inteiroNaoNegativo,
  juntarErros,
  obrigatorio,
  preco as validarPreco,
  textoObrigatorio,
  textoOpcional,
} from '../../lib/validacao'
import type { Categoria } from '../../types/categoria'
import type { Produto } from '../../types/produto'
import styles from './Produtos.module.css'

const formatarPreco = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

type FiltroAtivo = 'todos' | 'ativos' | 'inativos'

interface Rascunho {
  nome: string
  descricao: string
  preco: string
  quantidade: string
  categoriaId: string
  imagem: string | null
  disponivelNaCesta: boolean
  ativo: boolean
}

const RASCUNHO_VAZIO: Rascunho = {
  nome: '',
  descricao: '',
  preco: '',
  quantidade: '0',
  categoriaId: '',
  imagem: null,
  disponivelNaCesta: false,
  ativo: true,
}

function rascunhoDe(produto: Produto): Rascunho {
  return {
    nome: produto.nome,
    descricao: produto.descricao ?? '',
    preco: produto.preco.toFixed(2),
    quantidade: String(produto.quantidade),
    categoriaId: produto.categoriaId,
    imagem: produto.imagem,
    disponivelNaCesta: produto.disponivelNaCesta,
    ativo: produto.ativo,
  }
}

export function AdminProdutos() {
  const recurso = useRecursoAdmin<Produto, ProdutoEntrada>(
    {
      listar: listarProdutos,
      criar: criarProduto,
      atualizar: atualizarProduto,
      excluir: excluirProduto,
      idDe: (produto) => produto.id,
    },
    {
      criado: 'Produto criado',
      atualizado: 'Produto atualizado',
      excluido: 'Produto removido do catálogo',
    },
  )

  const [categorias, setCategorias] = useState<Categoria[]>([])

  useEffect(() => {
    let ativo = true
    listarCategorias()
      .then((encontradas) => {
        if (ativo) setCategorias(encontradas)
      })
      .catch(() => {
      })
    return () => {
      ativo = false
    }
  }, [])

  const nomePorCategoria = useMemo(() => {
    const mapa = new Map<string, string>()
    for (const categoria of categorias) mapa.set(categoria.id, categoria.nome)
    return mapa
  }, [categorias])

  const [filtroCategoria, setFiltroCategoria] = useState('todas')
  const [filtroAtivo, setFiltroAtivo] = useState<FiltroAtivo>('todos')

  const visiveis = useMemo(() => {
    return recurso.registros.filter((produto) => {
      if (filtroCategoria !== 'todas' && produto.categoriaId !== filtroCategoria) return false
      if (filtroAtivo === 'ativos' && !produto.ativo) return false
      if (filtroAtivo === 'inativos' && produto.ativo) return false
      return true
    })
  }, [recurso.registros, filtroCategoria, filtroAtivo])

  const [rascunho, setRascunho] = useState<Rascunho>(RASCUNHO_VAZIO)

  useEffect(() => {
    if (recurso.emEdicao === null) return
    setRascunho(
      recurso.registroEmEdicao ? rascunhoDe(recurso.registroEmEdicao) : RASCUNHO_VAZIO,
    )
  }, [recurso.emEdicao, recurso.registroEmEdicao])

  function alterar<C extends keyof Rascunho>(campo: C, valor: Rascunho[C]) {
    setRascunho((atual) => ({ ...atual, [campo]: valor }))
  }

  async function salvar() {
    const erros = juntarErros({
      nome: textoObrigatorio(rascunho.nome, LIMITES.nome),
      descricao: textoOpcional(rascunho.descricao, LIMITES.descricao),
      preco: validarPreco(rascunho.preco),
      quantidade: inteiroNaoNegativo(rascunho.quantidade),
      categoriaId: obrigatorio(rascunho.categoriaId),
    })

    await recurso.salvar(
      {
        nome: rascunho.nome.trim(),
        descricao: rascunho.descricao.trim() || null,
        preco: Number(rascunho.preco),
        quantidade: Number(rascunho.quantidade),
        categoriaId: rascunho.categoriaId,
        imagem: rascunho.imagem,
        disponivelNaCesta: rascunho.disponivelNaCesta,
        ativo: rascunho.ativo,
      },
      erros,
    )
  }

  const colunas: ColunaAdmin<Produto>[] = [
    { chave: 'nome', rotulo: 'Nome', conteudo: (produto) => produto.nome },
    {
      chave: 'categoria',
      rotulo: 'Categoria',
      conteudo: (produto) => nomePorCategoria.get(produto.categoriaId) ?? '—',
    },
    {
      chave: 'preco',
      rotulo: 'Preço',
      estreita: true,
      conteudo: (produto) => formatarPreco.format(produto.preco),
    },
    {
      chave: 'quantidade',
      rotulo: 'Estoque',
      estreita: true,
      conteudo: (produto) =>
        produto.quantidade === 0 ? (
          <span className={styles.esgotado}>Esgotado</span>
        ) : (
          produto.quantidade
        ),
    },
    {
      chave: 'cesta',
      rotulo: 'Na cesta',
      estreita: true,
      conteudo: (produto) => (produto.disponivelNaCesta ? 'Sim' : 'Não'),
    },
  ]

  return (
    <>
      <PaginaAdmin
        titulo="Produtos"
        descricao="Cadastro, preço, estoque e disponibilidade no montador de cesta."
        rotuloNovo="Novo produto"
        aoCriar={recurso.abrirCriacao}
        carregando={recurso.carregando}
        erro={recurso.erroCarregar}
        aoTentarDeNovo={recurso.recarregar}
        totalVisivel={visiveis.length}
        vazio={
          recurso.registros.length === 0
            ? 'Nenhum produto cadastrado ainda.'
            : 'Nenhum produto corresponde aos filtros.'
        }
        filtros={
          <>
            <label className={styles.filtro}>
              <span>Categoria</span>
              <select
                value={filtroCategoria}
                onChange={(evento) => setFiltroCategoria(evento.target.value)}
              >
                <option value="todas">Todas</option>
                {categorias.map((categoria) => (
                  <option key={categoria.id} value={categoria.id}>
                    {categoria.nome}
                    {categoria.ativo ? '' : ' (inativa)'}
                  </option>
                ))}
              </select>
            </label>

            <label className={styles.filtro}>
              <span>Situação</span>
              <select
                value={filtroAtivo}
                onChange={(evento) => setFiltroAtivo(evento.target.value as FiltroAtivo)}
              >
                <option value="todos">Todas</option>
                <option value="ativos">Só ativos</option>
                <option value="inativos">Só inativos</option>
              </select>
            </label>
          </>
        }
      >
        <TabelaAdmin
          registros={visiveis}
          colunas={colunas}
          idDe={(produto) => produto.id}
          nomeDe={(produto) => produto.nome}
          imagemDe={(produto) => produto.imagem}
          ativoDe={(produto) => produto.ativo}
          aoEditar={recurso.abrirEdicao}
          aoExcluir={recurso.pedirExclusao}
        />
      </PaginaAdmin>

      {recurso.emEdicao !== null && (
        <ModalFormulario
          titulo={recurso.registroEmEdicao ? 'Editar produto' : 'Novo produto'}
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

          <div className={styles.dupla}>
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

            <CampoFormulario rotulo="Estoque" erro={recurso.errosCampo.quantidade}>
              <input
                type="number"
                inputMode="numeric"
                step="1"
                min="0"
                value={rascunho.quantidade}
                onChange={(evento) => alterar('quantidade', evento.target.value)}
                disabled={recurso.salvando}
              />
            </CampoFormulario>
          </div>

          <CampoFormulario rotulo="Categoria" erro={recurso.errosCampo.categoriaId}>
            <select
              value={rascunho.categoriaId}
              onChange={(evento) => alterar('categoriaId', evento.target.value)}
              disabled={recurso.salvando}
            >
              <option value="">Selecione…</option>
              {categorias.map((categoria) => (
                <option key={categoria.id} value={categoria.id}>
                  {categoria.nome}
                  {categoria.ativo ? '' : ' (inativa)'}
                </option>
              ))}
            </select>
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
              checked={rascunho.disponivelNaCesta}
              onChange={(evento) => alterar('disponivelNaCesta', evento.target.checked)}
              disabled={recurso.salvando}
            />
            <span>
              Disponível no montador de cesta
              <small>A cliente pode escolher este produto para montar a cesta dela.</small>
            </span>
          </label>

          <label className={styles.marcador}>
            <input
              type="checkbox"
              checked={rascunho.ativo}
              onChange={(evento) => alterar('ativo', evento.target.checked)}
              disabled={recurso.salvando}
            />
            <span>
              Ativo
              <small>Desmarcado, o produto sai do catálogo público e continua aqui.</small>
            </span>
          </label>
        </ModalFormulario>
      )}

      {recurso.emExclusao && (
        <ConfirmacaoExclusao
          titulo="Remover produto?"
          descricao="O produto sai do catálogo público, mas continua aqui como inativo — o histórico é preservado e dá para reativá-lo depois."
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
