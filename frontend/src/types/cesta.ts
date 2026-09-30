import type { Produto } from './produto'

/** Um produto escolhido pela cliente no montador, com a quantidade dela. */
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
}
