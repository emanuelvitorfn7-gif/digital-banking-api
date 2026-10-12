import { http, HttpResponse } from 'msw'
import type {
  ClienteResponse,
  ContaResponse,
  PageResponse,
  TransacaoResponse,
} from '../api/types'

const API = 'http://localhost:5173/api'

function authHeader(username: string, password: string): string {
  return `Basic ${btoa(`${username}:${password}`)}`
}

const ADMIN_AUTH = authHeader('admin', 'admin123')
const CLIENTE_AUTH = authHeader('cliente', 'cliente123')

const STATUS_ERROR: Record<number, string> = {
  400: 'Bad Request',
  401: 'Unauthorized',
  403: 'Forbidden',
  404: 'Not Found',
  409: 'Conflict',
}

function erro(status: number, message: string, details: string[] = []) {
  return {
    timestamp: new Date().toISOString(),
    status,
    error: STATUS_ERROR[status] ?? 'Error',
    message,
    details,
  }
}

const clienteAna: ClienteResponse = {
  id: 1,
  nome: 'Ana Silva',
  cpf: '***.***.789-09',
  email: 'an***@email.com',
}

const contaUm: ContaResponse = {
  id: 1,
  numero: '00000001',
  saldo: 150,
  clienteId: 1,
  clienteNome: 'Ana Silva',
}

const deposito: TransacaoResponse = {
  id: 10,
  tipo: 'DEPOSITO',
  valor: 50,
  dataHora: '2026-10-11T17:51:00',
  contaOrigemId: null,
  contaDestinoId: 1,
}

const saque: TransacaoResponse = {
  id: 11,
  tipo: 'SAQUE',
  valor: 20,
  dataHora: '2026-10-11T18:10:00',
  contaOrigemId: 1,
  contaDestinoId: null,
}

const transferencia: TransacaoResponse = {
  id: 12,
  tipo: 'TRANSFERENCIA',
  valor: 30,
  dataHora: '2026-10-11T18:30:00',
  contaOrigemId: 1,
  contaDestinoId: 2,
}

const extrato: PageResponse<TransacaoResponse> = {
  content: [saque, deposito],
  page: 0,
  size: 10,
  totalElements: 2,
  totalPages: 1,
}

export const handlers = [
  // Probe de credenciais: precisa vir antes de "/contas/:id".
  http.get(`${API}/contas/0`, ({ request }) => {
    const auth = request.headers.get('Authorization')
    if (auth === ADMIN_AUTH || auth === CLIENTE_AUTH) {
      return HttpResponse.json(erro(404, 'Conta não encontrada.'), { status: 404 })
    }
    return new HttpResponse(null, { status: 401 })
  }),

  http.post(`${API}/clientes`, async ({ request }) => {
    const body = (await request.json()) as { nome: string; cpf: string; email: string }
    const criado: ClienteResponse = {
      id: 2,
      nome: body.nome,
      cpf: '***.***.009-99',
      email: 'no***@email.com',
    }
    return HttpResponse.json(criado, { status: 201 })
  }),

  http.get(`${API}/clientes/:id`, ({ params }) => {
    if (params.id === '1') return HttpResponse.json(clienteAna)
    return HttpResponse.json(erro(404, 'Cliente não encontrado.'), { status: 404 })
  }),

  http.post(`${API}/contas`, () =>
    HttpResponse.json({ ...contaUm, id: 5, numero: '00000042', saldo: 0 }, { status: 201 }),
  ),

  http.get(`${API}/contas/:id`, ({ params }) => {
    if (params.id === '1') return HttpResponse.json(contaUm)
    return HttpResponse.json(erro(404, 'Conta não encontrada.'), { status: 404 })
  }),

  http.post(`${API}/contas/:id/depositos`, () => HttpResponse.json(deposito)),

  http.post(`${API}/contas/:id/saques`, () => HttpResponse.json(saque)),

  http.post(`${API}/transferencias`, () => HttpResponse.json(transferencia)),

  http.get(`${API}/contas/:id/extrato`, () => HttpResponse.json(extrato)),
]
