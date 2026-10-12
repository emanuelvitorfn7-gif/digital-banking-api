import { useEffect, useState } from 'react'
import { contasApi } from '../../api/endpoints'
import type { ContaResponse } from '../../api/types'
import { ContaIdPicker } from '../../components/ContaIdPicker'
import { ContaCard } from '../../components/data/ContaCard'
import { ApiErrorAlert } from '../../components/ui/ApiErrorAlert'
import { Card, CardHeader } from '../../components/ui/Card'
import { EmptyState } from '../../components/ui/EmptyState'
import { LinkButton } from '../../components/ui/LinkButton'
import { Spinner } from '../../components/ui/Spinner'
import { useRequest } from '../../hooks/useRequest'

export function DashboardPage() {
  const [contaId, setContaId] = useState<number | null>(null)
  const { status, data, error, run, reset } = useRequest<ContaResponse>()

  useEffect(() => {
    if (contaId === null) return
    void run(() => contasApi.buscarPorId(contaId))
  }, [contaId, run])

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Painel</h1>
        <p className="text-sm text-slate-500">
          Consulte saldo, titular e acesse depósito, saque e extrato.
        </p>
      </div>

      <Card>
        <CardHeader
          title="Consultar conta"
          description="Informe o identificador da conta para carregar os dados."
        />
        <ContaIdPicker
          onSelect={(id) => {
            setContaId(id)
            if (id === contaId) {
              void run(() => contasApi.buscarPorId(id))
            }
          }}
        />
      </Card>

      {contaId === null ? (
        <EmptyState
          title="Nenhuma conta carregada"
          description="Informe um id de conta para ver o saldo, o titular e os atalhos de operação."
        />
      ) : null}

      {status === 'loading' ? (
        <div className="flex items-center gap-3 text-sm text-slate-500">
          <Spinner className="h-5 w-5 text-brand-600" />
          Carregando conta #{contaId}...
        </div>
      ) : null}

      {status === 'error' ? (
        <div className="flex flex-col gap-3">
          <ApiErrorAlert error={error} />
          <div className="flex gap-3">
            <button
              type="button"
              className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              onClick={() => {
                if (contaId !== null) void run(() => contasApi.buscarPorId(contaId))
              }}
            >
              Tentar novamente
            </button>
            <button
              type="button"
              className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
              onClick={() => {
                setContaId(null)
                reset()
              }}
            >
              Trocar conta
            </button>
          </div>
        </div>
      ) : null}

      {status === 'success' && data ? (
        <div className="flex flex-col gap-4">
          <ContaCard conta={data} />
          <div className="flex flex-wrap gap-3">
            <LinkButton to={`/contas/${data.id}/deposito`}>Depositar</LinkButton>
            <LinkButton to={`/contas/${data.id}/saque`} variant="secondary">
              Sacar
            </LinkButton>
            <LinkButton to={`/contas/${data.id}/extrato`} variant="secondary">
              Ver extrato
            </LinkButton>
            {data.clienteId !== null ? (
              <LinkButton to={`/clientes/${data.clienteId}`} variant="secondary">
                Ver titular
              </LinkButton>
            ) : null}
          </div>
        </div>
      ) : null}
    </div>
  )
}
