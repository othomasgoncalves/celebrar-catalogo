import { createContext, useContext, useEffect, useMemo, useReducer } from 'react'
import type { ReactNode } from 'react'
import type { ItemCesta } from '../types/cesta'
import type { Produto } from '../types/produto'

const CHAVE_STORAGE = 'celebrar:cesta'

/** Teto por item, só para a cliente não digitar um número sem sentido no stepper. */
export const QUANTIDADE_MAXIMA = 99

type ItensCesta = Map<string, ItemCesta>

type CestaAction =
  | { type: 'adicionar'; produto: Produto }
  | { type: 'remover'; produtoId: string }
  | { type: 'alterarQuantidade'; produtoId: string; quantidade: number }
  | { type: 'limpar' }

interface CestaContextValue {
  itens: ItensCesta
  totalItens: number
  totalPreco: number
  adicionar: (produto: Produto) => void
  remover: (produtoId: string) => void
  alterarQuantidade: (produtoId: string, quantidade: number) => void
  limpar: () => void
}

const CestaContext = createContext<CestaContextValue | null>(null)

/**
 * Itens que entram na conta. Um produto pode esgotar depois de ter sido
 * escolhido (ou depois de ter ficado guardado no localStorage), e nesse caso
 * ele continua visível na tela mas não soma no total nem vai no pedido.
 */
export function itensContabilizaveis(itens: ItensCesta): ItemCesta[] {
  return [...itens.values()].filter((item) => !item.produto.esgotado)
}

function reducer(itens: ItensCesta, action: CestaAction): ItensCesta {
  switch (action.type) {
    case 'adicionar': {
      const atual = itens.get(action.produto.id)
      const quantidade = Math.min((atual?.quantidade ?? 0) + 1, QUANTIDADE_MAXIMA)
      // Regrava o produto para o item guardado acompanhar preço/nome atuais.
      return new Map(itens).set(action.produto.id, { produto: action.produto, quantidade })
    }

    case 'remover': {
      if (!itens.has(action.produtoId)) return itens
      const proximo = new Map(itens)
      proximo.delete(action.produtoId)
      return proximo
    }

    case 'alterarQuantidade': {
      const atual = itens.get(action.produtoId)
      if (!atual) return itens

      const quantidade = Math.min(Math.trunc(action.quantidade), QUANTIDADE_MAXIMA)
      if (quantidade < 1) return reducer(itens, { type: 'remover', produtoId: action.produtoId })
      if (quantidade === atual.quantidade) return itens

      return new Map(itens).set(action.produtoId, { ...atual, quantidade })
    }

    case 'limpar':
      return itens.size === 0 ? itens : new Map()
  }
}

function ehItemValido(valor: unknown): valor is ItemCesta {
  if (typeof valor !== 'object' || valor === null) return false

  const item = valor as { produto?: unknown; quantidade?: unknown }
  if (typeof item.quantidade !== 'number' || !Number.isFinite(item.quantidade) || item.quantidade < 1) {
    return false
  }
  if (typeof item.produto !== 'object' || item.produto === null) return false

  const produto = item.produto as { id?: unknown; nome?: unknown; preco?: unknown }
  return (
    typeof produto.id === 'string' &&
    typeof produto.nome === 'string' &&
    typeof produto.preco === 'number' &&
    Number.isFinite(produto.preco)
  )
}

/**
 * Lê a cesta guardada. Qualquer coisa estranha no localStorage (JSON quebrado,
 * formato antigo, chave mexida na mão) vira cesta vazia — nunca uma tela branca.
 */
function carregar(): ItensCesta {
  try {
    const bruto = localStorage.getItem(CHAVE_STORAGE)
    if (!bruto) return new Map()

    const lido: unknown = JSON.parse(bruto)
    if (!Array.isArray(lido)) return new Map()

    const itens: ItensCesta = new Map()
    for (const candidato of lido) {
      if (!ehItemValido(candidato)) continue
      itens.set(candidato.produto.id, {
        produto: candidato.produto,
        quantidade: Math.min(Math.trunc(candidato.quantidade), QUANTIDADE_MAXIMA),
      })
    }
    return itens
  } catch {
    return new Map()
  }
}

function salvar(itens: ItensCesta) {
  try {
    localStorage.setItem(CHAVE_STORAGE, JSON.stringify([...itens.values()]))
  } catch {
    // Storage cheio ou bloqueado (aba privada): a cesta segue só em memória.
  }
}

interface CestaProviderProps {
  children: ReactNode
}

export function CestaProvider({ children }: CestaProviderProps) {
  const [itens, dispatch] = useReducer(reducer, undefined, carregar)

  useEffect(() => {
    salvar(itens)
  }, [itens])

  const valor = useMemo<CestaContextValue>(() => {
    const contabilizaveis = itensContabilizaveis(itens)

    return {
      itens,
      totalItens: contabilizaveis.reduce((soma, item) => soma + item.quantidade, 0),
      totalPreco: contabilizaveis.reduce((soma, item) => soma + item.produto.preco * item.quantidade, 0),
      adicionar: (produto) => dispatch({ type: 'adicionar', produto }),
      remover: (produtoId) => dispatch({ type: 'remover', produtoId }),
      alterarQuantidade: (produtoId, quantidade) =>
        dispatch({ type: 'alterarQuantidade', produtoId, quantidade }),
      limpar: () => dispatch({ type: 'limpar' }),
    }
  }, [itens])

  return <CestaContext.Provider value={valor}>{children}</CestaContext.Provider>
}

export function useCesta(): CestaContextValue {
  const contexto = useContext(CestaContext)
  if (!contexto) {
    throw new Error('useCesta precisa estar dentro de <CestaProvider>')
  }
  return contexto
}
