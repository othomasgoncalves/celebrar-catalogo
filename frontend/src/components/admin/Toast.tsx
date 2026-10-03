import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import type { ReactNode } from 'react'
import styles from './Toast.module.css'

const DURACAO_MS = 3200

interface ToastContextValue {
  avisar: (mensagem: string) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

interface ToastProviderProps {
  children: ReactNode
}

export function ToastProvider({ children }: ToastProviderProps) {
  const [mensagem, setMensagem] = useState<string | null>(null)
  const temporizador = useRef<number | null>(null)

  const limpar = useCallback(() => {
    if (temporizador.current !== null) {
      window.clearTimeout(temporizador.current)
      temporizador.current = null
    }
  }, [])

  const avisar = useCallback(
    (texto: string) => {
      limpar()
      setMensagem(texto)
      temporizador.current = window.setTimeout(() => setMensagem(null), DURACAO_MS)
    },
    [limpar],
  )

  useEffect(() => limpar, [limpar])

  const valor = useMemo<ToastContextValue>(() => ({ avisar }), [avisar])

  return (
    <ToastContext.Provider value={valor}>
      {children}
      <div className={styles.area} aria-live="polite" aria-atomic="true">
        {mensagem && <p className={styles.toast}>{mensagem}</p>}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast(): ToastContextValue {
  const contexto = useContext(ToastContext)
  if (!contexto) {
    throw new Error('useToast precisa estar dentro de <ToastProvider>')
  }
  return contexto
}
