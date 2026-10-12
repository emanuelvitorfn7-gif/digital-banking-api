import type { TransacaoResponse } from '../../api/types'
import { formatCurrency, formatDateTime } from '../../lib/formatters'
import { Alert } from '../ui/Alert'

export function TransacaoResult({ titulo, transacao }: { titulo: string; transacao: TransacaoResponse }) {
  return (
    <Alert variant="success" title={titulo}>
      <ul className="space-y-1">
        <li>
          Id da transação: <strong>#{transacao.id}</strong>
        </li>
        <li>
          Tipo: <strong>{transacao.tipo}</strong>
        </li>
        <li>
          Valor: <strong>{formatCurrency(transacao.valor)}</strong>
        </li>
        <li>
          Data: <strong>{formatDateTime(transacao.dataHora)}</strong>
        </li>
        {transacao.contaOrigemId !== null ? <li>Conta de origem: #{transacao.contaOrigemId}</li> : null}
        {transacao.contaDestinoId !== null ? <li>Conta de destino: #{transacao.contaDestinoId}</li> : null}
      </ul>
    </Alert>
  )
}
