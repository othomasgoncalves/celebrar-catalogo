const COOKIE_CSRF = 'XSRF-TOKEN'
const HEADER_CSRF = 'X-XSRF-TOKEN'

const METODOS_SEM_CSRF = new Set(['GET', 'HEAD', 'OPTIONS'])

const MENSAGENS_POR_STATUS: Record<number, string> = {
  0: 'Não foi possível falar com o servidor. Verifique sua conexão.',
  403: 'Ação não permitida. Recarregue a página e tente de novo.',
  404: 'Recurso não encontrado.',
  500: 'Erro inesperado no servidor. Tente novamente em instantes.',
}

export class ErroApi extends Error {
  readonly status: number
  readonly campos?: Record<string, string>

  constructor(mensagem: string, status: number, campos?: Record<string, string>) {
    super(mensagem)
    this.name = 'ErroApi'
    this.status = status
    this.campos = campos
  }
}

type TratadorSessaoExpirada = () => void

let tratadores: TratadorSessaoExpirada[] = []

export function registrarTratadorDeSessaoExpirada(tratador: TratadorSessaoExpirada): () => void {
  tratadores.push(tratador)
  return () => {
    tratadores = tratadores.filter((registrado) => registrado !== tratador)
  }
}

function lerCookie(nome: string): string | null {
  for (const parte of document.cookie.split('; ')) {
    const separador = parte.indexOf('=')
    if (separador === -1) continue
    if (parte.slice(0, separador) === nome) {
      return decodeURIComponent(parte.slice(separador + 1))
    }
  }
  return null
}

interface OpcoesRequisicao {
  metodo?: string
  corpo?: unknown
  tratarSessaoExpirada?: boolean
}

async function pedir<T>(caminho: string, opcoes: OpcoesRequisicao = {}): Promise<T> {
  const metodo = (opcoes.metodo ?? 'GET').toUpperCase()
  const temCorpo = opcoes.corpo !== undefined

  const headers = new Headers()
  if (temCorpo) {
    headers.set('Content-Type', 'application/json')
  }
  if (!METODOS_SEM_CSRF.has(metodo)) {
    const csrf = lerCookie(COOKIE_CSRF)
    if (csrf) headers.set(HEADER_CSRF, csrf)
  }

  let resposta: Response
  try {
    resposta = await fetch(caminho, {
      method: metodo,
      credentials: 'include',
      headers,
      body: temCorpo ? JSON.stringify(opcoes.corpo) : undefined,
    })
  } catch {
    throw new ErroApi(MENSAGENS_POR_STATUS[0], 0)
  }

  if (resposta.status === 401 && (opcoes.tratarSessaoExpirada ?? true)) {
    for (const tratador of [...tratadores]) tratador()
  }

  if (!resposta.ok) {
    throw await montarErro(resposta)
  }

  return (await lerCorpo(resposta)) as T
}

async function lerCorpo(resposta: Response): Promise<unknown> {
  if (resposta.status === 204 || resposta.headers.get('Content-Length') === '0') {
    return undefined
  }
  const texto = await resposta.text()
  if (!texto) return undefined
  return JSON.parse(texto)
}

async function montarErro(resposta: Response): Promise<ErroApi> {
  let mensagem =
    MENSAGENS_POR_STATUS[resposta.status] ?? `Não foi possível concluir a ação (${resposta.status}).`
  let campos: Record<string, string> | undefined

  try {
    const corpo: unknown = JSON.parse(await resposta.text())
    if (corpo && typeof corpo === 'object') {
      const { erro, campos: camposRecebidos } = corpo as {
        erro?: unknown
        campos?: unknown
      }
      if (typeof erro === 'string' && erro.trim()) mensagem = erro
      if (camposRecebidos && typeof camposRecebidos === 'object') {
        campos = camposRecebidos as Record<string, string>
      }
    }
  } catch {

  }

  return new ErroApi(mensagem, resposta.status, campos)
}

export function apiGet<T>(caminho: string, opcoes?: Pick<OpcoesRequisicao, 'tratarSessaoExpirada'>) {
  return pedir<T>(caminho, { ...opcoes, metodo: 'GET' })
}

export function apiPost<T>(caminho: string, corpo?: unknown) {
  return pedir<T>(caminho, { metodo: 'POST', corpo })
}

export function apiPut<T>(caminho: string, corpo?: unknown) {
  return pedir<T>(caminho, { metodo: 'PUT', corpo })
}

export function apiDelete<T>(caminho: string) {
  return pedir<T>(caminho, { metodo: 'DELETE' })
}
