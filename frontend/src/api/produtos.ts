import type { Produto } from '../types/produto'
import { apiGet } from './client'

export function fetchProdutos(): Promise<Produto[]> {
  return apiGet<Produto[]>('/api/produtos')
}

export function fetchProdutosDaCesta(): Promise<Produto[]> {
  return apiGet<Produto[]>('/api/produtos?disponivelNaCesta=true')
}
