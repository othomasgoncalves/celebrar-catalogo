import type { ReactNode } from 'react'
import type { Produto } from '../../types/produto'
import { urlImagem } from '../../lib/imagens'
import { IconCoracao } from '../icons/IconCoracao'
import styles from './CardProduto.module.css'

const TONS = ['t1', 't2', 't3']

const formatarPreco = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

interface CardProdutoProps {
  produto: Produto
  toneIndex: number
  animationDelayMs: number
  children?: ReactNode
}

export function CardProduto({ produto, toneIndex, animationDelayMs, children }: CardProdutoProps) {
  const tom = TONS[toneIndex % TONS.length]

  return (
    <article
      className={`${styles.card} rise ${produto.esgotado ? styles.esgotado : ''}`}
      style={{ animationDelay: `${animationDelayMs}ms` }}
      aria-disabled={produto.esgotado || undefined}
    >
      <div className={`${styles.thumb} ${tom}`}>
        {produto.esgotado && <span className={styles.selo}>Esgotado</span>}
        {produto.imagem ? (
          <img src={urlImagem(produto.imagem)} alt={produto.nome} />
        ) : (
          <IconCoracao />
        )}
      </div>
      <h3>{produto.nome}</h3>
      <p className={styles.desc}>{produto.descricao}</p>
      <p className={styles.preco}>{formatarPreco.format(produto.preco)}</p>
      {children}
    </article>
  )
}
