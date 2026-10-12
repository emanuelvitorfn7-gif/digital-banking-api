export interface ContaRequest {
  titular: string
}

export interface ContaResponse {
  id: number
  numero: string
  titular: string
  saldo: number
}

export interface SaldoResponse {
  id: number
  numero: string
  saldo: number
}

export interface ValorRequest {
  valor: number
}

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  details: string[]
}
