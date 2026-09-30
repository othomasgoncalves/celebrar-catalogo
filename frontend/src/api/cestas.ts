import type { Cesta } from '../types/cesta'
import { apiGet } from './client'

export function fetchCestas(): Promise<Cesta[]> {
  return apiGet<Cesta[]>('/api/cestas')
}
