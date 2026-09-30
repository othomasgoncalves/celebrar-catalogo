import type { UsuarioSessao } from '../types/usuario'
import { apiGet, apiPost } from './client'

export function fetchUsuarioLogado(): Promise<UsuarioSessao> {
  return apiGet<UsuarioSessao>('/api/auth/eu', { tratarSessaoExpirada: false })
}

export function login(email: string, senha: string): Promise<UsuarioSessao> {
  return apiPost<UsuarioSessao>('/api/auth/login', { email, senha })
}

export function logout(): Promise<void> {
  return apiPost<void>('/api/auth/logout')
}
