import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { server } from '../mocks/server'
import { apiFetch } from './client'

const API = 'http://localhost:5173/api'

describe('apiFetch', () => {
  it('retorna o corpo JSON em caso de sucesso', async () => {
    const conta = await apiFetch<{ id: number; numero: string }>('/contas/1')
    expect(conta.id).toBe(1)
    expect(conta.numero).toBe('00000001')
  })

  it('lanca ApiError com a mensagem retornada pela API em conflito (409)', async () => {
    server.use(
      http.post(`${API}/transferencias`, () =>
        HttpResponse.json(
          {
            timestamp: '2026-10-11T00:00:00Z',
            status: 409,
            error: 'Conflict',
            message: 'Saldo insuficiente para realizar a operação.',
            details: [],
          },
          { status: 409 },
        ),
      ),
    )

    await expect(apiFetch('/transferencias', { method: 'POST', body: {} })).rejects.toMatchObject({
      status: 409,
    })
    await expect(apiFetch('/transferencias', { method: 'POST', body: {} })).rejects.toThrow(
      'Saldo insuficiente',
    )
  })

  it('mapeia 401 sem corpo para uma mensagem padrao', async () => {
    server.use(http.get(`${API}/contas/1`, () => new HttpResponse(null, { status: 401 })))

    await expect(apiFetch('/contas/1')).rejects.toThrow('Não autenticado')
  })
})
