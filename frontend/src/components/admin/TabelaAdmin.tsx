import type { ReactNode } from 'react'
import { urlImagem } from '../../lib/imagens'
import { IconCoracao } from '../icons/IconCoracao'
import styles from './TabelaAdmin.module.css'

export interface ColunaAdmin<T> {
  chave: string
  rotulo: string
  conteudo: (registro: T) => ReactNode
  estreita?: boolean
}

interface TabelaAdminProps<T> {
  registros: T[]
  colunas: ColunaAdmin<T>[]
  idDe: (registro: T) => string
  nomeDe: (registro: T) => string
  imagemDe?: (registro: T) => string | null
  ativoDe: (registro: T) => boolean
  aoEditar: (registro: T) => void
  aoExcluir: (registro: T) => void
  rotuloExcluir?: string
}

export function TabelaAdmin<T>({
  registros,
  colunas,
  idDe,
  nomeDe,
  imagemDe,
  ativoDe,
  aoEditar,
  aoExcluir,
  rotuloExcluir = 'Excluir',
}: TabelaAdminProps<T>) {
  return (
    <div className={styles.moldura}>
      <table className={styles.tabela}>
        <thead>
          <tr>
            {imagemDe && (
              <th scope="col" className={styles.colunaImagem}>
                <span className={styles.oculto}>Imagem</span>
              </th>
            )}
            {colunas.map((coluna) => (
              <th
                key={coluna.chave}
                scope="col"
                className={coluna.estreita ? styles.colunaEstreita : undefined}
              >
                {coluna.rotulo}
              </th>
            ))}
            <th scope="col" className={styles.colunaSituacao}>
              Situação
            </th>
            <th scope="col" className={styles.colunaAcoes}>
              <span className={styles.oculto}>Ações</span>
            </th>
          </tr>
        </thead>

        <tbody>
          {registros.map((registro) => {
            const ativo = ativoDe(registro)
            const nome = nomeDe(registro)
            const imagem = imagemDe?.(registro) ?? null

            return (
              <tr key={idDe(registro)} className={ativo ? undefined : styles.linhaInativa}>
                {imagemDe && (
                  <td className={styles.celulaImagem}>
                    <div className={`${styles.thumb} t3`}>
                      {imagem ? <img src={urlImagem(imagem)} alt="" /> : <IconCoracao />}
                    </div>
                  </td>
                )}

                {colunas.map((coluna) => (
                  <td key={coluna.chave} data-rotulo={coluna.rotulo}>
                    {coluna.conteudo(registro)}
                  </td>
                ))}

                <td data-rotulo="Situação">
                  <span className={ativo ? styles.seloAtivo : styles.seloInativo}>
                    {ativo ? 'Ativo' : 'Inativo'}
                  </span>
                </td>

                <td className={styles.celulaAcoes}>
                  <div className={styles.acoes}>
                    <button
                      type="button"
                      className={styles.editar}
                      onClick={() => aoEditar(registro)}
                    >
                      Editar
                      <span className={styles.oculto}> {nome}</span>
                    </button>
                    <button
                      type="button"
                      className={styles.excluir}
                      onClick={() => aoExcluir(registro)}
                    >
                      {rotuloExcluir}
                      <span className={styles.oculto}> {nome}</span>
                    </button>
                  </div>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
