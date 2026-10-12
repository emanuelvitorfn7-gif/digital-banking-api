import { useCallback, useState } from 'react'

export type RequestStatus = 'idle' | 'loading' | 'success' | 'error'

export interface RequestState<T> {
  status: RequestStatus
  data: T | null
  error: Error | null
}

/**
 * Pequeno hook para padronizar estados de carregamento/erro/sucesso.
 */
export function useRequest<T>() {
  const [state, setState] = useState<RequestState<T>>({
    status: 'idle',
    data: null,
    error: null,
  })

  const run = useCallback(async (action: () => Promise<T>): Promise<T | null> => {
    setState({ status: 'loading', data: null, error: null })
    try {
      const data = await action()
      setState({ status: 'success', data, error: null })
      return data
    } catch (error) {
      setState({
        status: 'error',
        data: null,
        error: error instanceof Error ? error : new Error(String(error)),
      })
      return null
    }
  }, [])

  const reset = useCallback(() => {
    setState({ status: 'idle', data: null, error: null })
  }, [])

  return { ...state, run, reset }
}
