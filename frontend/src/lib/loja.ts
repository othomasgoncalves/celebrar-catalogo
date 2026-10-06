/**
 * Dados de contato da loja, em um lugar só. Antes o telefone e o nome viviam
 * repetidos em Header, Footer e Sobre, e o número do WhatsApp em whatsapp.ts —
 * trocar um e esquecer o outro era questão de tempo.
 */

export const LOJA = {
  nome: 'Celebrar',

  /** Só dígitos, com DDI, no formato que o wa.me espera. */
  whatsapp: '554439000663',

  telefone: {
    exibicao: '(44) 3900-0663',
    link: 'tel:+554439000663',
  },

  endereco: {
    logradouro: 'Av. Paraná, 1309',
    bairro: 'Jardim América',
    cidade: 'Paranavaí',
    uf: 'PR',
    cep: '87705-190',
  },

  horario: 'Segunda a sábado, 9h às 18h',
  entrega: 'Paranavaí e região, sob consulta',
} as const

/** "Av. Paraná, 1309 — Jardim América, Paranavaí - PR, 87705-190" */
export function enderecoCompleto(): string {
  const { logradouro, bairro, cidade, uf, cep } = LOJA.endereco
  return `${logradouro} — ${bairro}, ${cidade} - ${uf}, ${cep}`
}

/** Abre o endereço no app de mapas do aparelho (Google Maps no Android/desktop). */
export function urlMapa(): string {
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
    `${LOJA.nome}, ${enderecoCompleto()}`,
  )}`
}
