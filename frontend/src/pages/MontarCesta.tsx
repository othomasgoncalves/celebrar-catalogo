import { useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchProdutosDaCesta } from '../api/produtos'
import { CardProduto } from '../components/CardProduto/CardProduto'
import { CATEGORIA_TODOS, FiltroCategorias } from '../components/FiltroCategorias/FiltroCategorias'
import { IconWhatsapp } from '../components/icons/IconWhatsapp'
import { QUANTIDADE_MAXIMA, itensContabilizaveis, useCesta } from '../context/CestaContext'
import { useFetch } from '../hooks/useFetch'
import { buildMensagemCesta, buildWhatsappUrl } from '../lib/whatsapp'
import type { Produto } from '../types/produto'
import styles from './MontarCesta.module.css'

const SKELETON_COUNT = 6

const formatarPreco = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export function MontarCesta() {
  const [categoriaAtiva, setCategoriaAtiva] = useState<string>(CATEGORIA_TODOS)
  const { data: produtos, loading, error } = useFetch(fetchProdutosDaCesta)
  const { itens, totalItens, totalPreco, adicionar, alterarQuantidade } = useCesta()

  const produtosFiltrados = produtos?.filter(
    (produto) => categoriaAtiva === CATEGORIA_TODOS || produto.categoriaId === categoriaAtiva,
  )

  const cestaVazia = totalItens === 0

  return (
    <div className={styles.pagina}>
      <header className={styles.cabecalho}>
        <Link className={styles.voltar} to="/" aria-label="Voltar para a página inicial">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M19 12H6M11 18l-6-6 6-6" />
          </svg>
        </Link>
        <div className={styles.cabecalhoTxt}>
          <h1>Montar cesta</h1>
          <span>Escolha os itens e a quantidade</span>
        </div>
      </header>

      <FiltroCategorias ativo={categoriaAtiva} onChange={setCategoriaAtiva} />

      <div className={styles.secGrid}>
        {loading && (
          <div className={styles.grid} aria-hidden="true">
            {Array.from({ length: SKELETON_COUNT }).map((_, i) => (
              <div key={i}>
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
                >
                  <Stepper
                    produto={produto}
                    quantidade={itens.get(produto.id)?.quantidade ?? 0}
                    onAdicionar={adicionar}
                    onAlterarQuantidade={alterarQuantidade}
                  />
                </CardProduto>
              ))}
            </div>
          ) : (
            <p className={styles.mensagem}>Nenhum item nesta categoria por enquanto.</p>
          )
        )}
      </div>

      <div className={styles.barra}>
        {cestaVazia ? (
          <div className={styles.barraVazia}>
            <strong>Sua cesta está vazia</strong>
            <span>Toque no + dos itens que você quer levar.</span>
          </div>
        ) : (
          <>
            <div className={styles.resumo}>
              <span className={styles.contagem}>
                {totalItens === 1 ? '1 item' : `${totalItens} itens`}
              </span>
              <strong className={styles.total}>{formatarPreco.format(totalPreco)}</strong>
            </div>
            <a
              className={styles.enviar}
              href={buildWhatsappUrl(buildMensagemCesta(itensContabilizaveis(itens), totalPreco))}
              target="_blank"
              rel="noreferrer"
            >
              <IconWhatsapp />
              Enviar no WhatsApp
            </a>
          </>
        )}
      </div>
    </div>
  )
}

interface StepperProps {
  produto: Produto
  quantidade: number
  onAdicionar: (produto: Produto) => void
  onAlterarQuantidade: (produtoId: string, quantidade: number) => void
}

function Stepper({ produto, quantidade, onAdicionar, onAlterarQuantidade }: StepperProps) {
  if (produto.esgotado) {
    return <p className={styles.indisponivel}>Indisponível agora</p>
  }

  return (
    <div className={styles.stepper}>
      <button
        className={styles.passo}
        onClick={() => onAlterarQuantidade(produto.id, quantidade - 1)}
        disabled={quantidade === 0}
        aria-label={`Tirar uma unidade de ${produto.nome}`}
      >
        −
      </button>
      <span className={styles.quantidade} aria-live="polite" aria-label={`${quantidade} na cesta`}>
        {quantidade}
      </span>
      <button
        className={styles.passo}
        onClick={() => onAdicionar(produto)}
        disabled={quantidade >= QUANTIDADE_MAXIMA}
        aria-label={`Adicionar uma unidade de ${produto.nome}`}
      >
        +
      </button>
    </div>
  )
}
