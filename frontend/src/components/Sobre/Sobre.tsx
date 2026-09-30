import styles from './Sobre.module.css'

export function Sobre() {
  return (
    <section className={styles.sobre}>
      <h2>Sobre a Celebrar</h2>
      <p>
        A gente cuida da parte boa da festa: o doce que todo mundo comenta e o enfeite que faz a mesa virar foto.
        Cada cesta é montada na hora, com carinho e do jeito que você pediu.
      </p>
      <div className={styles.infos}>
        <div className={styles.info}>
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 2a10 10 0 100 20 10 10 0 000-20zm1 10.6V6h-2v7.4l5.2 3.1 1-1.7-4.2-2.2z" />
          </svg>
          <div>
            <b>Horário</b>
            <span>Segunda a sábado, 9h às 18h</span>
          </div>
        </div>
        <div className={styles.info}>
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 2C8.1 2 5 5.1 5 9c0 5.2 7 13 7 13s7-7.8 7-13c0-3.9-3.1-7-7-7zm0 9.5A2.5 2.5 0 1112 6.5a2.5 2.5 0 010 5z" />
          </svg>
          <div>
            <b>Entrega</b>
            <span>Paranavaí e região, sob consulta</span>
          </div>
        </div>
        <div className={styles.info}>
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 2a10 10 0 00-8.6 15L2 22l5.2-1.4A10 10 0 1012 2z" />
          </svg>
          <div>
            <b>Pedidos</b>
            <span>(44) 99999-0000</span>
          </div>
        </div>
      </div>
    </section>
  )
}
