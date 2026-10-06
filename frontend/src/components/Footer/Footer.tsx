import marca from '../../assets/marca.png'
import { IconWhatsapp } from '../icons/IconWhatsapp'
import { buildWhatsappUrl } from '../../lib/whatsapp'
import { LOJA, enderecoCompleto, urlMapa } from '../../lib/loja'
import styles from './Footer.module.css'

export function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.conteudo}>
        <img className={styles.marca} src={marca} alt={LOJA.nome} />
        <div className={styles.slogan}>Feito para celebrar</div>
        <a
          className={styles.fzap}
          href={buildWhatsappUrl(`Olá! Quero fazer um pedido na ${LOJA.nome}.`)}
          target="_blank"
          rel="noreferrer"
          aria-label="Falar no WhatsApp"
        >
          <IconWhatsapp />
          Falar no WhatsApp
        </a>

        <address className={styles.contato}>
          <a className={styles.linha} href={urlMapa()} target="_blank" rel="noreferrer">
            {enderecoCompleto()}
          </a>
          <a className={styles.linha} href={LOJA.telefone.link}>
            {LOJA.telefone.exibicao}
          </a>
        </address>

        <div className={styles.fim}>
          {LOJA.horario}
          <br />
          {LOJA.entrega}
        </div>
      </div>
    </footer>
  )
}
