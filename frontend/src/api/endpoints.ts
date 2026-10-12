import { ApiError, apiFetch } from './client'
import type { Credentials } from './credentials'
import type {
  ClienteRequest,
  ClienteResponse,
  ContaRequest,
  ContaResponse,
  PageResponse,
  TransacaoResponse,
  TransferenciaRequest,
  ValorRequest,
} from './types'

export const clientesApi = {
  criar: (body: ClienteRequest) => apiFetch<ClienteResponse>('/clientes', { method: 'POST', body }),
  buscarPorId: (id: number) => apiFetch<ClienteResponse>(`/clientes/${id}`),
}

export const contasApi = {
  abrir: (body: ContaRequest) => apiFetch<ContaResponse>('/contas', { method: 'POST', body }),
  buscarPorId: (id: number) => apiFetch<ContaResponse>(`/contas/${id}`),
  depositar: (id: number, valor: number) =>
    apiFetch<TransacaoResponse>(`/contas/${id}/depositos`, {
      method: 'POST',
      body: { valor } satisfies ValorRequest,
    }),
  sacar: (id: number, valor: number) =>
    apiFetch<TransacaoResponse>(`/contas/${id}/saques`, {
      method: 'POST',
      body: { valor } satisfies ValorRequest,
    }),
  extrato: (id: number, page: number, size: number) =>
    apiFetch<PageResponse<TransacaoResponse>>(`/contas/${id}/extrato`, { query: { page, size } }),
}

export const transferenciasApi = {
  transferir: (body: TransferenciaRequest) =>
    apiFetch<TransacaoResponse>('/transferencias', { method: 'POST', body }),
}

/**
 * A API nao possui endpoint de login. Para validar as credenciais demonstrativas
 * usamos um probe SOMENTE-LEITURA em um id inexistente: a API responde 401 quando
 * as credenciais sao invalidas e 404 (conta nao encontrada) quando sao validas.
 * Nenhum dado e alterado.
 */
export async function verificarCredenciais(credentials: Credentials): Promise<boolean> {
  try {
    await apiFetch<ContaResponse>('/contas/0', { auth: credentials })
    return true
  } catch (error) {
    if (error instanceof ApiError) {
      return error.status !== 401
    }
    throw error
  }
}
