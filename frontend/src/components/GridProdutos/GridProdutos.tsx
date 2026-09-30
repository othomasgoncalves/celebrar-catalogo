import { fetchProdutos } from '../../api/produtos'
import { useFetch } from '../../hooks/useFetch'
import { CATEGORIA_TODOS } from '../FiltroCategorias/FiltroCategorias'
import { CardProduto } from '../CardProduto/CardProduto'
import styles from './GridProdutos.module.css'

const SKELETON_COUNT = 8

interface GridProdutosProps {
  categoriaAtiva: string
}

export function GridProdutos({ categoriaAtiva }: GridProdutosProps) {
  const { data: produtos, loading, error } = useFetch(fetchProdutos)

  const produtosFiltrados = produtos?.filter(
    (produto) => categoriaAtiva === CATEGORIA_TODOS || produto.categoriaId === categoriaAtiva,
  )

  return (
    <div className={styles.secGrid}>
      {loading && (
        <div className={styles.grid} aria-hidden="true">
          {Array.from({ length: SKELETON_COUNT }).map((_, i) => (
            <div key={i} className={styles.skeletonCard}>
              <div className={styles.skeletonThumb} />
              <div className={styles.skeletonLine} style={{ width: '85%' }} />
              <div className={styles.skeletonLine} style={{ width: '55%' }} />
            </div>
          ))}
        </div>
      )}

      {error && <p className={styles.mensagem}>Não foi possível carregar os produtos agora.</p>}

      {!loading && !error && produtosFiltrados && (
        produtosFiltrados.length > 0 ? (
          <div className={styles.grid}>
            {produtosFiltrados.map((produto, i) => (
              <CardProduto
                key={produto.id}
                produto={produto}
                toneIndex={i}
                animationDelayMs={60 + i * 45}
              />
            ))}
          </div>
        ) : (
          <p className={styles.mensagem}>Nenhum item nesta categoria por enquanto.</p>
        )
      )}
    </div>
  )
}
