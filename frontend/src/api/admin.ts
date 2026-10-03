import type { Categoria } from '../types/categoria'
import type { Cesta } from '../types/cesta'
import type { Produto } from '../types/produto'
import { apiDelete, apiGet, apiPost, apiPut, apiUpload } from './client'

export interface ProdutoEntrada {
  nome: string
  descricao: string | null
  preco: number
  quantidade: number
  categoriaId: string
  imagem: string | null
  disponivelNaCesta: boolean
  ativo: boolean
}

export interface CestaEntrada {
  nome: string
  descricao: string | null
  preco: number
  itens: string
  imagem: string | null
  ativo: boolean
}

export interface CategoriaEntrada {
  nome: string
  ordem: number
  ativo: boolean
}

export function listarProdutos(): Promise<Produto[]> {
  return apiGet<Produto[]>('/api/admin/produtos')
}

export function criarProduto(dados: ProdutoEntrada): Promise<Produto> {
  return apiPost<Produto>('/api/admin/produtos', dados)
}

export function atualizarProduto(id: string, dados: ProdutoEntrada): Promise<Produto> {
  return apiPut<Produto>(`/api/admin/produtos/${id}`, dados)
}

export function excluirProduto(id: string): Promise<void> {
  return apiDelete<void>(`/api/admin/produtos/${id}`)
}

export function listarCestas(): Promise<Cesta[]> {
  return apiGet<Cesta[]>('/api/admin/cestas')
}

export function criarCesta(dados: CestaEntrada): Promise<Cesta> {
  return apiPost<Cesta>('/api/admin/cestas', dados)
}

export function atualizarCesta(id: string, dados: CestaEntrada): Promise<Cesta> {
  return apiPut<Cesta>(`/api/admin/cestas/${id}`, dados)
}

export function excluirCesta(id: string): Promise<void> {
  return apiDelete<void>(`/api/admin/cestas/${id}`)
}

export function listarCategorias(): Promise<Categoria[]> {
  return apiGet<Categoria[]>('/api/admin/categorias')
}

export function criarCategoria(dados: CategoriaEntrada): Promise<Categoria> {
  return apiPost<Categoria>('/api/admin/categorias', dados)
}

export function atualizarCategoria(id: string, dados: CategoriaEntrada): Promise<Categoria> {
  return apiPut<Categoria>(`/api/admin/categorias/${id}`, dados)
}

export function excluirCategoria(id: string): Promise<void> {
  return apiDelete<void>(`/api/admin/categorias/${id}`)
}

interface ImagemCriada {
  nome: string
}

export async function enviarImagem(
  arquivo: File,
  aoProgredir?: (fracao: number) => void,
): Promise<string> {
  const { nome } = await apiUpload<ImagemCriada>('/api/admin/imagens', 'arquivo', arquivo, {
    aoProgredir,
  })
  return nome
}
