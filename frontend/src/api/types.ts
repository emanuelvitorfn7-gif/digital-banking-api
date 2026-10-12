export type TipoTransacao = 'DEPOSITO' | 'SAQUE' | 'TRANSFERENCIA'

export interface ClienteRequest {
  nome: string
  cpf: string
  email: string
}

/** Cliente com CPF e e-mail ja mascarados pelo Back-End. */
export interface ClienteResponse {
  id: number
  nome: string
  cpf: string
  email: string
}

export interface ContaRequest {
  clienteId: number
}

export interface ContaResponse {
  id: number
  numero: string
  saldo: number
  clienteId: number | null
  clienteNome: string | null
}

export interface ValorRequest {
  valor: number
}

export interface TransferenciaRequest {
  contaOrigemId: number
  contaDestinoId: number
  valor: number
}

export interface TransacaoResponse {
  id: number
  tipo: TipoTransacao
  valor: number
  dataHora: string
  contaOrigemId: number | null
  contaDestinoId: number | null
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  details: string[]
}
