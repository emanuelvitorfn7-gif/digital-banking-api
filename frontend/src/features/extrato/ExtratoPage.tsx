import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { contasApi } from '../../api/endpoints'
import type { PageResponse, TransacaoResponse } from '../../api/types'
import { ContaIdPicker } from '../../components/ContaIdPicker'
import { ApiErrorAlert } from '../../components/ui/ApiErrorAlert'
import { Button } from '../../components/ui/Button'
import { Card, CardHeader } from '../../components/ui/Card'
import { EmptyState } from '../../components/ui/EmptyState'
import { Spinner } from '../../components/ui/Spinner'
import { useRequest } from '../../hooks/useRequest'
import { formatCurrency, formatDateTime } from '../../lib/formatters'

const PAGE_SIZE = 10

function TipoBadge({ tipo }: { tipo: TransacaoResponse['tipo'] }) {
  const classes: Record<TransacaoResponse['tipo'], string> = {
    DEPOSITO: 'bg-emerald-100 text-emerald-800',
    SAQUE: 'bg-amber-100 text-amber-800',
    TRANSFERENCIA: 'bg-brand-100 text-brand-800',
  }
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${classes[tipo]}`}>
      {tipo}
    </span>
  )
}

export function ExtratoPage() {
  const { id } = useParams()
  const [contaId, setContaId] = useState<number | null>(id && /^\d+$/.test(id) ? Number(id) : null)
  const [page, setPage] = useState(0)
  const { status, data, error, run } = useRequest<PageResponse<TransacaoResponse>>()

  useEffect(() => {
    if (contaId === null) return
    void run(() => contasApi.extrato(contaId, page, PAGE_SIZE))
  }, [contaId, page, run])

  if (contaId === null) {
    return (
      <div className="mx-auto max-w-xl">
        <Card>
          <CardHeader title="Extrato" description="Informe a conta para consultar as transações." />
          <ContaIdPicker onSelect={setContaId} />
        </Card>
      </div>
    )
  }

  const totalPages = data?.totalPages ?? 0
  const currentPage = data?.page ?? page

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Extrato da conta #{contaId}</h1>
          <p className="text-sm text-slate-500">
            Transações em que a conta aparece como origem ou destino, da mais recente para a mais antiga.
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={() => { setContaId(null); setPage(0) }}>
          Trocar conta
        </Button>
      </div>

      {status === 'error' ? <ApiErrorAlert error={error} /> : null}

      {status === 'loading' ? (
        <div className="flex items-center gap-3 text-sm text-slate-500">
          <Spinner className="h-5 w-5 text-brand-600" />
          Carregando extrato...
        </div>
      ) : null}

      {status === 'success' && data && data.content.length === 0 ? (
        <EmptyState
          title="Sem transações"
          description="Esta conta ainda não possui depósitos, saques ou transferências registrados."
        />
      ) : null}

      {status === 'success' && data && data.content.length > 0 ? (
        <div className="overflow-x-auto rounded-2xl border border-slate-200 bg-white shadow-sm">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-3 font-medium">Data</th>
                <th className="px-4 py-3 font-medium">Tipo</th>
                <th className="px-4 py-3 text-right font-medium">Valor</th>
                <th className="px-4 py-3 font-medium">Origem</th>
                <th className="px-4 py-3 font-medium">Destino</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {data.content.map((transacao) => (
                <tr key={transacao.id} className="hover:bg-slate-50">
                  <td className="whitespace-nowrap px-4 py-3 text-slate-600">
                    {formatDateTime(transacao.dataHora)}
                  </td>
                  <td className="px-4 py-3">
                    <TipoBadge tipo={transacao.tipo} />
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right font-medium text-slate-900">
                    {formatCurrency(transacao.valor)}
                  </td>
                  <td className="px-4 py-3 text-slate-600">
                    {transacao.contaOrigemId !== null ? `#${transacao.contaOrigemId}` : '—'}
                  </td>
                  <td className="px-4 py-3 text-slate-600">
                    {transacao.contaDestinoId !== null ? `#${transacao.contaDestinoId}` : '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}

      {status === 'success' && data ? (
        <div className="flex items-center justify-between gap-3">
          <p className="text-sm text-slate-500">
            {data.totalElements} transação(ões) — página {totalPages === 0 ? 0 : currentPage + 1} de{' '}
            {Math.max(totalPages, 1)}
          </p>
          <div className="flex gap-2">
            <Button
              variant="secondary"
              size="sm"
              disabled={currentPage <= 0}
              onClick={() => setPage((value) => Math.max(0, value - 1))}
            >
              Anterior
            </Button>
            <Button
              variant="secondary"
              size="sm"
              disabled={currentPage + 1 >= totalPages}
              onClick={() => setPage((value) => value + 1)}
            >
              Próxima
            </Button>
          </div>
        </div>
      ) : null}
    </div>
  )
}
