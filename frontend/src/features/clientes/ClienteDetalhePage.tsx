import { useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { clientesApi } from '../../api/endpoints'
import type { ClienteResponse } from '../../api/types'
import { ApiErrorAlert } from '../../components/ui/ApiErrorAlert'
import { Card, CardHeader } from '../../components/ui/Card'
import { EmptyState } from '../../components/ui/EmptyState'
import { LinkButton } from '../../components/ui/LinkButton'
import { Spinner } from '../../components/ui/Spinner'
import { useRequest } from '../../hooks/useRequest'

export function ClienteDetalhePage() {
  const { id } = useParams()
  const clienteId = id && /^\d+$/.test(id) ? Number(id) : null
  const { status, data, error, run } = useRequest<ClienteResponse>()

  useEffect(() => {
    if (clienteId === null) return
    void run(() => clientesApi.buscarPorId(clienteId))
  }, [clienteId, run])

  if (clienteId === null) {
    return <EmptyState title="Id de cliente inválido" description="A rota deve conter um id numérico." />
  }

  return (
    <div className="mx-auto max-w-xl">
      <Card>
        <CardHeader title={`Cliente #${clienteId}`} description="Dados pessoais retornados mascarados pela API." />

        {status === 'loading' ? (
          <div className="flex items-center gap-3 text-sm text-slate-500">
            <Spinner className="h-5 w-5 text-brand-600" />
            Carregando cliente...
          </div>
        ) : null}

        {status === 'error' ? <ApiErrorAlert error={error} /> : null}

        {status === 'success' && data ? (
          <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <dt className="text-xs uppercase tracking-wide text-slate-500">Nome</dt>
              <dd className="text-sm font-medium text-slate-900">{data.nome}</dd>
            </div>
            <div>
              <dt className="text-xs uppercase tracking-wide text-slate-500">Id</dt>
              <dd className="text-sm font-medium text-slate-900">#{data.id}</dd>
            </div>
            <div>
              <dt className="text-xs uppercase tracking-wide text-slate-500">CPF</dt>
              <dd className="text-sm font-medium text-slate-900">{data.cpf}</dd>
            </div>
            <div>
              <dt className="text-xs uppercase tracking-wide text-slate-500">E-mail</dt>
              <dd className="text-sm font-medium text-slate-900">{data.email}</dd>
            </div>
            <div className="sm:col-span-2">
              <LinkButton to={`/contas/abrir?clienteId=${data.id}`} variant="secondary" size="sm">
                Abrir conta para este cliente
              </LinkButton>
            </div>
          </dl>
        ) : null}
      </Card>
    </div>
  )
}
