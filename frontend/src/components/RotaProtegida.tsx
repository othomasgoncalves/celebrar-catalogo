import type { ReactNode } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { ROTA_LOGIN, useAuth } from '../context/AuthContext'
import styles from './RotaProtegida.module.css'

interface RotaProtegidaProps {
  children?: ReactNode
}

export function RotaProtegida({ children }: RotaProtegidaProps) {
  const { usuario, carregando } = useAuth()
  const location = useLocation()

  if (carregando) {
    return (
      <div className={styles.verificando} role="status" aria-live="polite">
        <span className={styles.spinner} aria-hidden="true" />
        <span className={styles.rotulo}>Verificando sessão…</span>
      </div>
    )
  }

  if (!usuario) {
    return <Navigate to={ROTA_LOGIN} replace state={{ de: location.pathname + location.search }} />
  }

  return <>{children ?? <Outlet />}</>
}
