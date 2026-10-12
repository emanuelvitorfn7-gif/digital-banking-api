import { apiFetch } from './client'
import type { ContaRequest, ContaResponse, SaldoResponse, ValorRequest } from './types'

export const contasApi = {
  criar: (titular: string) =>
    apiFetch<ContaResponse>('/contas', { method: 'POST', body: { titular } satisfies ContaRequest }),

  listar: () => apiFetch<ContaResponse[]>('/contas'),

  buscar: (id: number) => apiFetch<ContaResponse>(`/contas/${id}`),

  saldo: (id: number) => apiFetch<SaldoResponse>(`/contas/${id}/saldo`),

  depositar: (id: number, valor: number) =>
    apiFetch<ContaResponse>(`/contas/${id}/deposito`, {
      method: 'POST',
      body: { valor } satisfies ValorRequest,
    }),

  sacar: (id: number, valor: number) =>
    apiFetch<ContaResponse>(`/contas/${id}/saque`, {
      method: 'POST',
      body: { valor } satisfies ValorRequest,
    }),
}
