import type { Produto } from '../types/produto'
import { apiGet } from './client'

export function fetchProdutos(): Promise<Produto[]> {
  return apiGet<Produto[]>('/api/produtos')
}

/** Só os produtos que a cliente pode escolher no montador de cesta. */
export function fetchProdutosDaCesta(): Promise<Produto[]> {
  return apiGet<Produto[]>('/api/produtos?disponivelNaCesta=true')
}
