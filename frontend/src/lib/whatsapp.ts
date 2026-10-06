import type { ItemCesta } from '../types/cesta'
import { LOJA } from './loja'

const formatarPreco = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export function buildWhatsappUrl(mensagem: string): string {
  return `https://wa.me/${LOJA.whatsapp}?text=${encodeURIComponent(mensagem)}`
}

export function buildMensagemCesta(itens: ItemCesta[], total: number): string {
  const linhas = itens.map(
    ({ produto, quantidade }) =>
      `• ${quantidade}x ${produto.nome} — ${formatarPreco.format(produto.preco * quantidade)}`,
  )

  return [
    'Olá! Quero montar uma cesta personalizada:',
    '',
    ...linhas,
    '',
    `Total: ${formatarPreco.format(total)}`,
  ].join('\n')
}
