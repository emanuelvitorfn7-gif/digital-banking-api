import { ApiError } from '../../api/client'
import { Alert } from './Alert'

export function ApiErrorAlert({ error }: { error: Error | null }) {
  if (!error) return null

  const isApiError = error instanceof ApiError
  return (
    <Alert variant="error" title={isApiError ? `Erro ${error.status}` : 'Erro'}>
      <p>{error.message}</p>
      {isApiError && error.status === 401 ? (
        <p className="mt-1">Sua sessão pode ter expirado. Saia e entre novamente.</p>
      ) : null}
      {isApiError && error.details.length > 0 ? (
        <ul className="mt-2 list-disc space-y-0.5 pl-5">
          {error.details.map((detail) => (
            <li key={detail}>{detail}</li>
          ))}
        </ul>
      ) : null}
    </Alert>
  )
}
