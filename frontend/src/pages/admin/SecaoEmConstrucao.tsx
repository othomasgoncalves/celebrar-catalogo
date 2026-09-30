import styles from './SecaoEmConstrucao.module.css'

interface SecaoEmConstrucaoProps {
  titulo: string
  descricao: string
}

/** Placeholder das seções do admin até o CRUD de cada uma entrar. */
export function SecaoEmConstrucao({ titulo, descricao }: SecaoEmConstrucaoProps) {
  return (
    <section>
      <h1 className={styles.titulo}>{titulo}</h1>
      <p className={styles.descricao}>{descricao}</p>
      <div className={styles.vazio}>Em construção.</div>
    </section>
  )
}
