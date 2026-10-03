import type { ReactNode } from 'react'
import styles from './PaginaAdmin.module.css'

interface PaginaAdminProps {
  titulo: string
  descricao: string
  rotuloNovo: string
  aoCriar: () => void
  carregando: boolean
  erro: string | null
  aoTentarDeNovo: () => void
  totalVisivel: number
  vazio: string
  filtros?: ReactNode
  children: ReactNode
}

export function PaginaAdmin({
  titulo,
  descricao,
  rotuloNovo,
  aoCriar,
  carregando,
  erro,
  aoTentarDeNovo,
  totalVisivel,
  vazio,
  filtros,
  children,
}: PaginaAdminProps) {
  return (
    <section>
      <header className={styles.cabecalho}>
        <div className={styles.textos}>
          <h1>{titulo}</h1>
          <p>{descricao}</p>
        </div>
        <button type="button" className={styles.novo} onClick={aoCriar} disabled={carregando}>
          {rotuloNovo}
        </button>
      </header>

      {filtros && <div className={styles.filtros}>{filtros}</div>}

      {carregando && (
        <div className={styles.esqueleto} aria-hidden="true">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className={styles.esqueletoLinha} />
          ))}
        </div>
      )}

      {!carregando && erro && (
        <div className={styles.erro} role="alert">
          <p>{erro}</p>
          <button type="button" onClick={aoTentarDeNovo}>
            Tentar de novo
          </button>
        </div>
      )}

      {!carregando && !erro && totalVisivel === 0 && <div className={styles.vazio}>{vazio}</div>}

      {!carregando && !erro && totalVisivel > 0 && children}
    </section>
  )
}
