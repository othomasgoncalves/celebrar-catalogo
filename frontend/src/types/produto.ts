export interface Produto {
  id: string
  nome: string
  descricao: string | null
  preco: number
  quantidade: number
  esgotado: boolean
  disponivelNaCesta: boolean
  categoriaId: string
  imagem: string | null
}
