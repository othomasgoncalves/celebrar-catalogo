import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { fetchUsuarioLogado, login as loginNaApi, logout as logoutNaApi } from '../api/auth'
import { ErroApi, registrarTratadorDeSessaoExpirada } from '../api/client'
import type { UsuarioSessao } from '../types/usuario'

export const ROTA_LOGIN = '/admin/login'

interface AuthContextValue {
  usuario: UsuarioSessao | null
  carregando: boolean
  entrar: (email: string, senha: string) => Promise<void>
  sair: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [usuario, setUsuario] = useState<UsuarioSessao | null>(null)
  const [carregando, setCarregando] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    let ativo = true

    fetchUsuarioLogado()
      .then((encontrado) => {
        if (ativo) setUsuario(encontrado)
      })
      .catch(() => {
        if (ativo) setUsuario(null)
      })
      .finally(() => {
        if (ativo) setCarregando(false)
      })

    return () => {
      ativo = false
    }
  }, [])

  useEffect(() => {
    return registrarTratadorDeSessaoExpirada(() => {
      setUsuario(null)
      const caminho = window.location.pathname
      if (caminho.startsWith('/admin') && caminho !== ROTA_LOGIN) {
        navigate(ROTA_LOGIN, { replace: true })
      }
    })
  }, [navigate])

  const entrar = useCallback(async (email: string, senha: string) => {
    const autenticado = await loginNaApi(email, senha)
    setUsuario(autenticado)
  }, [])

  const sair = useCallback(async () => {
    try {
      await logoutNaApi()
    } catch (erro) {
      if (!(erro instanceof ErroApi)) throw erro
    } finally {
      setUsuario(null)
    }
  }, [])

  const valor = useMemo<AuthContextValue>(
    () => ({ usuario, carregando, entrar, sair }),
    [usuario, carregando, entrar, sair],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth precisa estar dentro de <AuthProvider>')
  }
  return contexto
}
