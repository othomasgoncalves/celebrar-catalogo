import { useEffect, useId, useRef } from 'react'
import styles from './ConfirmacaoExclusao.module.css'

interface ConfirmacaoExclusaoProps {
  titulo: string
  descricao: string
  nome: string
  excluindo: boolean
  erro?: string | null
  aoConfirmar: () => void
  aoCancelar: () => void
}

export function ConfirmacaoExclusao({
  titulo,
  descricao,
  nome,
  excluindo,
  erro,
  aoConfirmar,
  aoCancelar,
}: ConfirmacaoExclusaoProps) {
  const idTitulo = useId()
  const cancelar = useRef<HTMLButtonElement>(null)
  const cancelarAcao = useRef(aoCancelar)
  const excluindoRef = useRef(excluindo)

  cancelarAcao.current = aoCancelar
  excluindoRef.current = excluindo

  useEffect(() => {
    function aoTeclar(evento: KeyboardEvent) {
      if (evento.key === 'Escape' && !excluindoRef.current) {
        evento.stopPropagation()
        cancelarAcao.current()
      }
    }

    document.addEventListener('keydown', aoTeclar)
    return () => document.removeEventListener('keydown', aoTeclar)
  }, [])

  useEffect(() => {
    cancelar.current?.focus()
  }, [])

  return (
    <div
      className={styles.fundo}
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget && !excluindo) aoCancelar()
      }}
    >
      <div className={styles.cartao} role="alertdialog" aria-modal="true" aria-labelledby={idTitulo}>
        <h2 className={styles.titulo} id={idTitulo}>
          {titulo}
        </h2>

        <p className={styles.descricao}>{descricao}</p>
        <p className={styles.nome}>{nome}</p>

        {erro && (
          <p className={styles.erro} role="alert">
            {erro}
          </p>
        )}

        <div className={styles.acoes}>
          <button
            ref={cancelar}
            type="button"
            className={styles.cancelar}
            onClick={aoCancelar}
            disabled={excluindo}
          >
            Cancelar
          </button>
          <button
            type="button"
            className={styles.excluir}
            onClick={aoConfirmar}
            disabled={excluindo}
          >
            {excluindo ? 'Excluindo…' : 'Excluir'}
          </button>
        </div>
      </div>
    </div>
  )
}
