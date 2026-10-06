export const LOJA = {
  nome: 'Celebrar',

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

export function enderecoCompleto(): string {
  const { logradouro, bairro, cidade, uf, cep } = LOJA.endereco
  return `${logradouro} — ${bairro}, ${cidade} - ${uf}, ${cep}`
}

export function urlMapa(): string {
  return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(
    `${LOJA.nome}, ${enderecoCompleto()}`,
  )}`
}
