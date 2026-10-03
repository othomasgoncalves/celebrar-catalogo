import type { Produto } from './produto'

export interface ItemCesta {
  produto: Produto
  quantidade: number
}

export interface Cesta {
  id: string
  nome: string
  descricao: string | null
  preco: number
  itens: string
  imagem: string | null
  ativo: boolean
}
