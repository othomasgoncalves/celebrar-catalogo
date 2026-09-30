import logo from '../../assets/logo.png'
import { IconWhatsapp } from '../icons/IconWhatsapp'
import { buildWhatsappUrl } from '../../lib/whatsapp'
import styles from './Header.module.css'

export function Header() {
  return (
    <header className={styles.header}>
      <div className={styles.hd}>
        <img src={logo} alt="Celebrar" />
        <a
          className={styles.zap}
          href={buildWhatsappUrl('Olá! Quero fazer um pedido na Celebrar Confeitaria.')}
          target="_blank"
          rel="noreferrer"
          aria-label="Pedir pelo WhatsApp"
        >
          <IconWhatsapp />
          Pedir
        </a>
      </div>
    </header>
  )
}
