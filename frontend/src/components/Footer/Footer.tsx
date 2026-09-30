import marca from '../../assets/marca.png'
import { IconWhatsapp } from '../icons/IconWhatsapp'
import { buildWhatsappUrl } from '../../lib/whatsapp'
import styles from './Footer.module.css'

export function Footer() {
  return (
    <footer className={styles.footer}>
      <img className={styles.marca} src={marca} alt="Celebrar" />
      <div className={styles.slogan}>Feito para celebrar</div>
      <a
        className={styles.fzap}
        href={buildWhatsappUrl('Olá! Quero fazer um pedido na Celebrar Confeitaria.')}
        target="_blank"
        rel="noreferrer"
        aria-label="Falar no WhatsApp"
      >
        <IconWhatsapp />
        Falar no WhatsApp
      </a>
      <div className={styles.fim}>
        Segunda a sábado, 9h às 18h
        <br />
        Paranavaí e região
      </div>
    </footer>
  )
}
