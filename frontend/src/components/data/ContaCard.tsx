import type { ContaResponse } from '../../api/types'
import { formatCurrency } from '../../lib/formatters'
import { Card } from '../ui/Card'

export function ContaCard({ conta }: { conta: ContaResponse }) {
  return (
    <Card>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-xs uppercase tracking-wide text-slate-500">Conta {conta.numero}</p>
          <p className="mt-1 text-3xl font-semibold text-slate-900">{formatCurrency(conta.saldo)}</p>
          <p className="mt-1 text-sm text-slate-500">
            Titular: {conta.clienteNome ?? '—'}
            {conta.clienteId !== null ? ` (#${conta.clienteId})` : ''}
          </p>
        </div>
        <div className="text-right">
          <p className="text-xs text-slate-400">Id interno</p>
          <p className="text-sm font-medium text-slate-600">#{conta.id}</p>
        </div>
      </div>
    </Card>
  )
}
