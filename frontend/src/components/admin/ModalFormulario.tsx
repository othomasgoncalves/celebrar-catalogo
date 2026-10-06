import { useEffect, useId, useRef } from 'react'
import type { FormEvent, ReactNode } from 'react'
import styles from './ModalFormulario.module.css'

interface ModalFormularioProps {
  titulo: string
  salvando: boolean
  rotuloSalvar?: string
  erro?: string | null
  aoFechar: () => void
  aoEnviar: () => void
  children: ReactNode
}

export function ModalFormulario({
  titulo,
  salvando,
  rotuloSalvar = 'Salvar',
  erro,
  aoFechar,
  aoEnviar,
  children,
}: ModalFormularioProps) {
  const idTitulo = useId()
  const cartao = useRef<HTMLFormElement>(null)
  const fecharRef = useRef(aoFechar)
  const salvandoRef = useRef(salvando)

  fecharRef.current = aoFechar
  salvandoRef.current = salvando

  useEffect(() => {
    function aoTeclar(evento: KeyboardEvent) {
      if (evento.key === 'Escape' && !salvandoRef.current) {
        evento.stopPropagation()
        fecharRef.current()
      }
    }

    document.addEventListener('keydown', aoTeclar)
    return () => document.removeEventListener('keydown', aoTeclar)
  }, [])

  useEffect(() => {
    const anterior = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = anterior
    }
  }, [])

  useEffect(() => {
    const primeiro = cartao.current?.querySelector<HTMLElement>(
      'input:not([type="hidden"]), select, textarea',
    )
    primeiro?.focus()
  }, [])

  function aoSubmeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (!salvando) aoEnviar()
  }

  return (
    <div
      className={styles.fundo}
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget && !salvando) aoFechar()
      }}
    >
      <form
        ref={cartao}
        className={styles.cartao}
        role="dialog"
        aria-modal="true"
        aria-labelledby={idTitulo}
        onSubmit={aoSubmeter}
        noValidate
      >
        <div className={styles.cabecalho}>
          <h2 id={idTitulo}>{titulo}</h2>
          <button
            type="button"
            className={styles.fechar}
            onClick={aoFechar}
            disabled={salvando}
            aria-label="Fechar"
          >
            ×
          </button>
        </div>

        <div className={styles.corpo}>{children}</div>

        {erro && (
          <p className={styles.erro} role="alert">
            {erro}
          </p>
        )}

        <div className={styles.acoes}>
          <button type="button" className={styles.cancelar} onClick={aoFechar} disabled={salvando}>
            Cancelar
          </button>
          <button type="submit" className={styles.salvar} disabled={salvando}>
            {salvando ? 'Salvando…' : rotuloSalvar}
          </button>
        </div>
      </form>
    </div>
  )
}
