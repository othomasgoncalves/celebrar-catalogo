import { useId } from 'react'
import type { ReactElement } from 'react'
import { cloneElement } from 'react'
import styles from './CampoFormulario.module.css'

interface CampoFormularioProps {
  rotulo: string
  erro?: string
  dica?: string
  children: ReactElement<{
    id?: string
    'aria-invalid'?: boolean
    'aria-describedby'?: string
  }>
}

export function CampoFormulario({ rotulo, erro, dica, children }: CampoFormularioProps) {
  const id = useId()
  const idErro = `${id}-erro`
  const idDica = `${id}-dica`

  const descricao = [erro ? idErro : null, dica ? idDica : null].filter(Boolean).join(' ')

  return (
    <div className={styles.campo}>
      <label className={styles.rotulo} htmlFor={id}>
        {rotulo}
      </label>

      {cloneElement(children, {
        id,
        'aria-invalid': erro ? true : undefined,
        'aria-describedby': descricao || undefined,
      })}

      {dica && !erro && (
        <p className={styles.dica} id={idDica}>
          {dica}
        </p>
      )}

      {erro && (
        <p className={styles.erro} id={idErro}>
          {erro}
        </p>
      )}
    </div>
  )
}
