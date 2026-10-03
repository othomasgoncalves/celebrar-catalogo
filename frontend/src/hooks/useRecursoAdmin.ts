import { useCallback, useEffect, useRef, useState } from 'react'
import { ErroApi } from '../api/client'
import { useToast } from '../components/admin/Toast'
import type { ErrosPorCampo } from '../lib/validacao'

interface Operacoes<T, E> {
  listar: () => Promise<T[]>
  criar: (dados: E) => Promise<unknown>
  atualizar: (id: string, dados: E) => Promise<unknown>
  excluir: (id: string) => Promise<void>
  idDe: (registro: T) => string
}

interface Mensagens {
  criado: string
  atualizado: string
  excluido: string
}

export function useRecursoAdmin<T, E>(operacoes: Operacoes<T, E>, mensagens: Mensagens) {
  const { avisar } = useToast()

  const [registros, setRegistros] = useState<T[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erroCarregar, setErroCarregar] = useState<string | null>(null)

  const [emEdicao, setEmEdicao] = useState<T | 'novo' | null>(null)
  const [salvando, setSalvando] = useState(false)
  const [errosCampo, setErrosCampo] = useState<ErrosPorCampo>({})
  const [erroFormulario, setErroFormulario] = useState<string | null>(null)

  const [emExclusao, setEmExclusao] = useState<T | null>(null)
  const [excluindo, setExcluindo] = useState(false)
  const [erroExclusao, setErroExclusao] = useState<string | null>(null)

  const operacoesRef = useRef(operacoes)
  const mensagensRef = useRef(mensagens)
  operacoesRef.current = operacoes
  mensagensRef.current = mensagens

  const carregar = useCallback(async () => {
    setCarregando(true)
    setErroCarregar(null)
    try {
      setRegistros(await operacoesRef.current.listar())
    } catch (causa) {
      setErroCarregar(
        causa instanceof Error ? causa.message : 'Não foi possível carregar a lista.',
      )
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    void carregar()
  }, [carregar])

  const abrirCriacao = useCallback(() => {
    setErrosCampo({})
    setErroFormulario(null)
    setEmEdicao('novo')
  }, [])

  const abrirEdicao = useCallback((registro: T) => {
    setErrosCampo({})
    setErroFormulario(null)
    setEmEdicao(registro)
  }, [])

  const fecharFormulario = useCallback(() => {
    setEmEdicao(null)
    setErrosCampo({})
    setErroFormulario(null)
  }, [])

  const salvar = useCallback(
    async (dados: E, errosLocais: ErrosPorCampo = {}): Promise<boolean> => {
      if (Object.keys(errosLocais).length > 0) {
        setErrosCampo(errosLocais)
        setErroFormulario(null)
        return false
      }

      setSalvando(true)
      setErrosCampo({})
      setErroFormulario(null)
      try {
        const { atualizar, criar, idDe } = operacoesRef.current
        const editando = emEdicao !== null && emEdicao !== 'novo'
        if (editando) {
          await atualizar(idDe(emEdicao), dados)
        } else {
          await criar(dados)
        }

        setEmEdicao(null)
        await carregar()
        avisar(editando ? mensagensRef.current.atualizado : mensagensRef.current.criado)
        return true
      } catch (causa) {
        if (causa instanceof ErroApi) {
          if (causa.campos) setErrosCampo(causa.campos)
          setErroFormulario(causa.campos ? null : causa.message)
        } else {
          setErroFormulario('Não foi possível salvar.')
        }
        return false
      } finally {
        setSalvando(false)
      }
    },
    [emEdicao, carregar, avisar],
  )

  const pedirExclusao = useCallback((registro: T) => {
    setErroExclusao(null)
    setEmExclusao(registro)
  }, [])

  const cancelarExclusao = useCallback(() => {
    setEmExclusao(null)
    setErroExclusao(null)
  }, [])

  const confirmarExclusao = useCallback(async () => {
    if (!emExclusao) return

    setExcluindo(true)
    setErroExclusao(null)
    try {
      const { excluir, idDe } = operacoesRef.current
      await excluir(idDe(emExclusao))
      setEmExclusao(null)
      await carregar()
      avisar(mensagensRef.current.excluido)
    } catch (causa) {
      setErroExclusao(
        causa instanceof Error ? causa.message : 'Não foi possível excluir.',
      )
    } finally {
      setExcluindo(false)
    }
  }, [emExclusao, carregar, avisar])

  return {
    registros,
    carregando,
    erroCarregar,
    recarregar: carregar,

    emEdicao,
    criando: emEdicao === 'novo',
    registroEmEdicao: emEdicao !== null && emEdicao !== 'novo' ? emEdicao : null,
    salvando,
    errosCampo,
    erroFormulario,
    abrirCriacao,
    abrirEdicao,
    fecharFormulario,
    salvar,

    emExclusao,
    excluindo,
    erroExclusao,
    pedirExclusao,
    cancelarExclusao,
    confirmarExclusao,
  }
}
