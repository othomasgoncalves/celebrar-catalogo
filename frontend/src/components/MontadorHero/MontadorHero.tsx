import { useNavigate } from 'react-router-dom'
import { IconCoracao } from '../icons/IconCoracao'
import styles from './MontadorHero.module.css'

export function MontadorHero() {
  const navigate = useNavigate()

  function handleComecarClick() {
    navigate('/montar')
  }

  return (
    <section className={`${styles.hero} rise`}>
      <div className={styles.ornament}>
        <IconCoracao />
      </div>
      <div className={styles.txt}>
        <span className={styles.tag}>Novidade</span>
        <h1 className={styles.titulo}>Monte sua cesta do seu jeito</h1>
        <p className={styles.descricao}>
          Escolha os itens que quiser, veja o total somando na hora e envie o pedido pronto pelo WhatsApp.
        </p>
        <button className={styles.cta} onClick={handleComecarClick}>
          Começar a montar
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M5 12h13M13 6l6 6-6 6" />
          </svg>
        </button>
      </div>
      <div className={styles.passos}>
        <div className={styles.passo}>
          <b>1. Escolha</b>
          <span>Doces, enfeites e variedades</span>
        </div>
        <div className={styles.passo}>
          <b>2. Some</b>
          <span>O total aparece na hora</span>
        </div>
        <div className={styles.passo}>
          <b>3. Envie</b>
          <span>A lista vai pronta no zap</span>
        </div>
      </div>
    </section>
  )
}
