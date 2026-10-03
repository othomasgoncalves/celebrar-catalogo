import { useId, useRef, useState } from 'react'
import type { ChangeEvent } from 'react'
import { enviarImagem } from '../../api/admin'
import { ErroApi } from '../../api/client'
import { urlImagem } from '../../lib/imagens'
import { IconCoracao } from '../icons/IconCoracao'
import styles from './CampoImagem.module.css'

const TIPOS_ACEITOS = 'image/jpeg,image/png,image/webp'

const TAMANHO_MAXIMO_BYTES = 3 * 1024 * 1024

interface CampoImagemProps {
  valor: string | null
  aoMudar: (nome: string | null) => void
  erro?: string
  desabilitado?: boolean
}

export function CampoImagem({ valor, aoMudar, erro, desabilitado }: CampoImagemProps) {
  const idArquivo = useId()
  const entrada = useRef<HTMLInputElement>(null)

  const [enviando, setEnviando] = useState(false)
  const [progresso, setProgresso] = useState(0)
  const [erroEnvio, setErroEnvio] = useState<string | null>(null)

  async function aoEscolher(evento: ChangeEvent<HTMLInputElement>) {
    const arquivo = evento.target.files?.[0]
    evento.target.value = ''
    if (!arquivo) return

    setErroEnvio(null)

    if (arquivo.size > TAMANHO_MAXIMO_BYTES) {
      setErroEnvio('A imagem passa de 3 MB. Escolha um arquivo menor.')
      return
    }

    setEnviando(true)
    setProgresso(0)
    try {
      const nome = await enviarImagem(arquivo, setProgresso)
      aoMudar(nome)
    } catch (causa) {
      setErroEnvio(
        causa instanceof ErroApi ? causa.message : 'Não foi possível enviar a imagem.',
      )
    } finally {
      setEnviando(false)
    }
  }

  const mensagem = erroEnvio ?? erro
  const ocupado = enviando || desabilitado

  return (
    <div className={styles.campo}>
      <span className={styles.rotulo}>Imagem</span>

      <div className={styles.linha}>
        <div className={`${styles.previa} t3`}>
          {valor ? (
            <img src={urlImagem(valor)} alt="Prévia da imagem selecionada" />
          ) : (
            <IconCoracao />
          )}
        </div>

        <div className={styles.controles}>
          <input
            ref={entrada}
            id={idArquivo}
            className={styles.arquivo}
            type="file"
            accept={TIPOS_ACEITOS}
            onChange={aoEscolher}
            disabled={ocupado}
            aria-invalid={mensagem ? true : undefined}
          />

          <div className={styles.botoes}>
            <button
              type="button"
              className={styles.escolher}
              onClick={() => entrada.current?.click()}
              disabled={ocupado}
            >
              {enviando ? 'Enviando…' : valor ? 'Trocar imagem' : 'Escolher imagem'}
            </button>

            {valor && !enviando && (
              <button
                type="button"
                className={styles.remover}
                onClick={() => {
                  setErroEnvio(null)
                  aoMudar(null)
                }}
                disabled={desabilitado}
              >
                Remover
              </button>
            )}
          </div>

          {enviando && (
            <div
              className={styles.barra}
              role="progressbar"
              aria-label="Envio da imagem"
              aria-valuemin={0}
              aria-valuemax={100}
              aria-valuenow={Math.round(progresso * 100)}
            >
              <div className={styles.barraInterna} style={{ width: `${progresso * 100}%` }} />
            </div>
          )}

          {!enviando && !mensagem && (
            <p className={styles.dica}>JPEG, PNG ou WebP, até 3 MB.</p>
          )}

          {mensagem && (
            <p className={styles.erro} role="alert">
              {mensagem}
            </p>
          )}
        </div>
      </div>
    </div>
  )
}
