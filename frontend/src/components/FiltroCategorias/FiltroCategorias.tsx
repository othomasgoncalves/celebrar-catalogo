import { fetchCategorias } from '../../api/categorias'
import { useFetch } from '../../hooks/useFetch'
import styles from './FiltroCategorias.module.css'

export const CATEGORIA_TODOS = 'todos'

interface FiltroCategoriasProps {
  ativo: string
  onChange: (categoriaId: string) => void
}

export function FiltroCategorias({ ativo, onChange }: FiltroCategoriasProps) {
  const { data: categorias, loading, error } = useFetch(fetchCategorias)

  return (
    <div className={styles.chips}>
      <button
        className={styles.chip}
        aria-pressed={ativo === CATEGORIA_TODOS}
        onClick={() => onChange(CATEGORIA_TODOS)}
      >
        Todos
      </button>
      {loading &&
        Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className={styles.skeletonChip} aria-hidden="true" />
        ))}
      {!loading &&
        !error &&
        categorias?.map((categoria) => (
          <button
            key={categoria.id}
            className={styles.chip}
            aria-pressed={ativo === categoria.id}
            onClick={() => onChange(categoria.id)}
          >
            {categoria.nome}
          </button>
        ))}
    </div>
  )
}
