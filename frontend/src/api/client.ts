import { getCredentials, type Credentials } from './credentials'
import type { ApiErrorResponse } from './types'

const BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/+$/, '')

const STATUS_MESSAGES: Record<number, string> = {
  400: 'Requisição inválida. Verifique os dados informados.',
  401: 'Não autenticado. Verifique usuário e senha.',
  403: 'Acesso negado. Seu perfil não permite esta operação.',
  404: 'Recurso não encontrado.',
  405: 'Método não permitido para este recurso.',
  409: 'Conflito com o estado atual do recurso.',
  500: 'Erro interno no servidor.',
}

export class ApiError extends Error {
  readonly status: number
  readonly details: string[]

  constructor(status: number, message: string, details: string[] = []) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  query?: Record<string, string | number | boolean | undefined | null>
  signal?: AbortSignal
  auth?: Credentials | null
}

function buildQuery(query: RequestOptions['query']): string {
  if (!query) return ''
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null || value === '') continue
    params.set(key, String(value))
  }
  const serialized = params.toString()
  return serialized ? `?${serialized}` : ''
}

function encodeBase64(value: string): string {
  const bytes = new TextEncoder().encode(value)
  let binary = ''
  for (const byte of bytes) {
    binary += String.fromCharCode(byte)
  }
  return btoa(binary)
}

export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, query, signal, auth } = options

  const headers: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  const credentials = auth === undefined ? getCredentials() : auth
  if (credentials) {
    headers.Authorization = `Basic ${encodeBase64(`${credentials.username}:${credentials.password}`)}`
  }

  const response = await fetch(`${BASE_URL}${path}${buildQuery(query)}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
    signal,
  })

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  let payload: unknown
  if (text.length > 0) {
    try {
      payload = JSON.parse(text)
    } catch {
      payload = undefined
    }
  }

  if (!response.ok) {
    const apiError = payload as Partial<ApiErrorResponse> | undefined
    const message =
      apiError && typeof apiError.message === 'string' && apiError.message.length > 0
        ? apiError.message
        : STATUS_MESSAGES[response.status] ?? `Erro inesperado (HTTP ${response.status}).`
    const details = apiError && Array.isArray(apiError.details) ? apiError.details : []
    throw new ApiError(response.status, message, details)
  }

  return payload as T
}
