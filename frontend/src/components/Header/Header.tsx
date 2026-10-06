import logo from '../../assets/logo.png'
import { IconWhatsapp } from '../icons/IconWhatsapp'
import { buildWhatsappUrl } from '../../lib/whatsapp'
import { LOJA } from '../../lib/loja'
import styles from './Header.module.css'

export function Header() {
  return (
    <header className={styles.header}>
      <div className={styles.hd}>
        <img src={logo} alt="Celebrar" />
        <a
          className={styles.zap}
          href={buildWhatsappUrl(`Olá! Quero fazer um pedido na ${LOJA.nome}.`)}
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
