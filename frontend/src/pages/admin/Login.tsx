import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import styles from './Login.module.css'

const ROTA_PADRAO = '/admin/produtos'

interface EstadoDeOrigem {
  de?: string
}

export function Login() {
  const { usuario, carregando, entrar } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const destino = (location.state as EstadoDeOrigem | null)?.de ?? ROTA_PADRAO

  if (carregando) {
    return null
  }

  if (usuario) {
    return <Navigate to={destino} replace />
  }

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (enviando) return

    setErro(null)
    setEnviando(true)
    try {
      await entrar(email.trim(), senha)
      navigate(destino, { replace: true })
    } catch (causa) {
      setErro(causa instanceof Error ? causa.message : 'Não foi possível entrar.')
      setSenha('')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className={styles.tela}>
      <form className={styles.cartao} onSubmit={aoEnviar} noValidate>
        <div className={styles.cabecalho}>
          <h1>Área administrativa</h1>
          <p>Entre para gerenciar o catálogo.</p>
        </div>

        <label className={styles.campo}>
          <span>E-mail</span>
          <input
            type="email"
            name="email"
            value={email}
            onChange={(evento) => setEmail(evento.target.value)}
            autoComplete="username"
            autoCapitalize="none"
            spellCheck={false}
            required
            autoFocus
            disabled={enviando}
          />
        </label>

        <label className={styles.campo}>
          <span>Senha</span>
          <input
            type="password"
            name="senha"
            value={senha}
            onChange={(evento) => setSenha(evento.target.value)}
            autoComplete="current-password"
            required
            disabled={enviando}
          />
        </label>

        {/* role=alert faz o leitor de tela anunciar a falha sem precisar de foco. */}
        {erro && (
          <p className={styles.erro} role="alert">
            {erro}
          </p>
        )}

        <button className={styles.enviar} type="submit" disabled={enviando}>
          {enviando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </main>
  )
}
