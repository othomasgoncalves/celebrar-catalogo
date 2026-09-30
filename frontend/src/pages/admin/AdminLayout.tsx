import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import styles from './AdminLayout.module.css'

const SECOES = [
  { para: '/admin/produtos', rotulo: 'Produtos' },
  { para: '/admin/cestas', rotulo: 'Cestas' },
  { para: '/admin/categorias', rotulo: 'Categorias' },
]

export function AdminLayout() {
  const { usuario, sair } = useAuth()
  const [saindo, setSaindo] = useState(false)

  async function aoSair() {
    if (saindo) return
    setSaindo(true)
    try {
      await sair()
    } finally {
      setSaindo(false)
    }
  }

  return (
    <div className={styles.shell}>
      <nav className={styles.barra} aria-label="Seções da administração">
        <span className={styles.marca}>Celebrar</span>

        <ul className={styles.secoes}>
          {SECOES.map((secao) => (
            <li key={secao.para}>
              <NavLink
                to={secao.para}
                className={({ isActive }) =>
                  isActive ? `${styles.link} ${styles.linkAtivo}` : styles.link
                }
              >
                {secao.rotulo}
              </NavLink>
            </li>
          ))}
        </ul>

        <div className={styles.conta}>
          <span className={styles.email} title={usuario?.email}>
            {usuario?.email}
          </span>
          <button type="button" className={styles.sair} onClick={aoSair} disabled={saindo}>
            {saindo ? 'Saindo…' : 'Sair'}
          </button>
        </div>
      </nav>

      <main className={styles.conteudo}>
        <Outlet />
      </main>
    </div>
  )
}
