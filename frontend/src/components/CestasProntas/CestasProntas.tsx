import { fetchCestas } from '../../api/cestas'
import { useFetch } from '../../hooks/useFetch'
import { IconCoracao } from '../icons/IconCoracao'
import { buildWhatsappUrl } from '../../lib/whatsapp'
import styles from './CestasProntas.module.css'

const TONS = ['t2', 't1', 't3']

const formatarPreco = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

const SKELETON_COUNT = 2

export function CestasProntas() {
  const { data: cestas, loading, error } = useFetch(fetchCestas)

  if (error) {
    return <p className={styles.mensagem}>Não foi possível carregar as cestas prontas agora.</p>
  }

  if (loading) {
    return (
      <div className={styles.cestas} aria-hidden="true">
        {Array.from({ length: SKELETON_COUNT }).map((_, i) => (
          <div key={i} className={styles.skeletonCesta}>
            <div className={styles.skeletonFoto} />
            <div className={styles.skeletonCorpo}>
              <div className={styles.skeletonLine} style={{ width: '70%' }} />
              <div className={styles.skeletonLine} style={{ width: '95%' }} />
              <div className={styles.skeletonLine} style={{ width: '85%' }} />
            </div>
          </div>
        ))}
      </div>
    )
  }

  if (!cestas || cestas.length === 0) {
    return <p className={styles.mensagem}>Nenhuma cesta pronta disponível no momento.</p>
  }

  return (
    <div className={styles.cestas}>
      {cestas.map((cesta, i) => {
        const tom = TONS[i % TONS.length]
        return (
          <article key={cesta.id} className={`${styles.cesta} rise`} style={{ animationDelay: `${120 + i * 70}ms` }}>
            <div className={`${styles.foto} ${tom}`}>
              {cesta.imagem ? <img src={cesta.imagem} alt={cesta.nome} /> : <IconCoracao />}
            </div>
            <div className={styles.corpo}>
              <h3>{cesta.nome}</h3>
              <p className={styles.itens}>{cesta.itens}</p>
              <div className={styles.rodape}>
                <span className={styles.preco}>{formatarPreco.format(cesta.preco)}</span>
                <a
                  className={styles.pedir}
                  href={buildWhatsappUrl(`Olá! Quero pedir a cesta "${cesta.nome}".`)}
                  target="_blank"
                  rel="noreferrer"
                  aria-label={`Pedir a cesta ${cesta.nome} pelo WhatsApp`}
                >
                  Pedir
                </a>
              </div>
            </div>
          </article>
        )
      })}
    </div>
  )
}
