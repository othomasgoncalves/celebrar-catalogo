export const LIMITES = {
  nome: 120,
  nomeCategoria: 60,
  descricao: 2000,
  itens: 2000,
  precoMaximo: 99_999_999.99,
} as const

export type ErrosPorCampo = Record<string, string>

export function textoObrigatorio(valor: string, limite: number): string | null {
  if (!valor.trim()) return 'não deve estar em branco'
  if (valor.trim().length > limite) return `deve ter no máximo ${limite} caracteres`
  return null
}

export function textoOpcional(valor: string, limite: number): string | null {
  if (valor.trim().length > limite) return `deve ter no máximo ${limite} caracteres`
  return null
}

export function preco(valor: string): string | null {
  if (!valor.trim()) return 'não deve ser nulo'

  const numero = Number(valor)
  if (!Number.isFinite(numero)) return 'informe um valor numérico'
  if (numero < 0) return 'deve ser maior que ou igual a 0,00'
  if (numero > LIMITES.precoMaximo) return 'valor acima do permitido'

  const decimais = valor.split('.')[1] ?? ''
  if (decimais.length > 2) return 'use no máximo 2 casas decimais'

  return null
}

export function inteiroNaoNegativo(valor: string): string | null {
  if (!valor.trim()) return 'não deve ser nulo'

  const numero = Number(valor)
  if (!Number.isInteger(numero)) return 'informe um número inteiro'
  if (numero < 0) return 'deve ser maior que ou igual a 0'

  return null
}

export function obrigatorio(valor: string): string | null {
  return valor.trim() ? null : 'não deve ser nulo'
}

export function juntarErros(entradas: Record<string, string | null>): ErrosPorCampo {
  const erros: ErrosPorCampo = {}
  for (const [campo, mensagem] of Object.entries(entradas)) {
    if (mensagem) erros[campo] = mensagem
  }
  return erros
}
