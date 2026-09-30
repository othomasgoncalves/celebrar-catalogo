import type { Categoria } from '../types/categoria'
import { apiGet } from './client'

export function fetchCategorias(): Promise<Categoria[]> {
  return apiGet<Categoria[]>('/api/categorias')
}
