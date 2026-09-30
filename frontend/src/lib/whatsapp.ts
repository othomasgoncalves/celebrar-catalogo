import type { ItemCesta } from '../types/cesta'

const NUMERO_WHATSAPP = '5544999990000'

const formatarPreco = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

export function buildWhatsappUrl(mensagem: string): string {
  return `https://wa.me/${NUMERO_WHATSAPP}?text=${encodeURIComponent(mensagem)}`
}

/**
 * Monta o texto do pedido da cesta personalizada. O valor de cada linha é o
 * subtotal do item (preço x quantidade), para as linhas fecharem com o total.
 */
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
