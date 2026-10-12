import { useState, type FormEvent } from 'react'
import { transferenciasApi } from '../../api/endpoints'
import type { TransacaoResponse } from '../../api/types'
import { TransacaoResult } from '../../components/data/TransacaoResult'
import { RequireRole } from '../../components/layout/RequireRole'
import { ApiErrorAlert } from '../../components/ui/ApiErrorAlert'
import { Button } from '../../components/ui/Button'
import { Card, CardHeader } from '../../components/ui/Card'
import { ConfirmModal } from '../../components/ui/ConfirmModal'
import { CurrencyInput } from '../../components/ui/CurrencyInput'
import { TextField } from '../../components/ui/TextField'
import { useRequest } from '../../hooks/useRequest'
import { formatCurrency } from '../../lib/formatters'
import { parseCurrencyInput } from '../../lib/masks'

interface FormErrors {
  origem?: string
  destino?: string
  valor?: string
}

function parseId(value: string): number | null {
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null
}

function TransferenciaForm() {
  const [origem, setOrigem] = useState('')
  const [destino, setDestino] = useState('')
  const [valor, setValor] = useState('')
  const [errors, setErrors] = useState<FormErrors>({})
  const [confirming, setConfirming] = useState(false)
  const [valorConfirmado, setValorConfirmado] = useState<number | null>(null)
  const { status, data, error, run } = useRequest<TransacaoResponse>()

  function validar(): { origemId: number; destinoId: number; valor: number } | null {
    const next: FormErrors = {}
    const origemId = parseId(origem)
    const destinoId = parseId(destino)
    const valorNumero = parseCurrencyInput(valor)

    if (origemId === null) next.origem = 'Informe um id de conta válido.'
    if (destinoId === null) next.destino = 'Informe um id de conta válido.'
    if (origemId !== null && destinoId !== null && origemId === destinoId) {
      next.destino = 'A conta de destino deve ser diferente da origem.'
    }
    if (valorNumero === null || valorNumero <= 0) next.valor = 'Informe um valor maior que zero.'

    setErrors(next)
    if (Object.keys(next).length > 0 || origemId === null || destinoId === null || valorNumero === null) {
      return null
    }
    return { origemId, destinoId, valor: valorNumero }
  }

  function handleReview(event: FormEvent) {
    event.preventDefault()
    const dados = validar()
    if (!dados) return
    setValorConfirmado(dados.valor)
    setConfirming(true)
  }

  async function handleConfirm() {
    if (valorConfirmado === null) return
    const result = await run(() =>
      transferenciasApi.transferir({
        contaOrigemId: Number(origem),
        contaDestinoId: Number(destino),
        valor: valorConfirmado,
      }),
    )
    if (result) {
      setConfirming(false)
      setValorConfirmado(null)
      setValor('')
    }
  }

  return (
    <div className="mx-auto max-w-xl">
      <Card>
        <CardHeader
          title="Transferência"
          description="Requer perfil ADMIN. Debita a origem e credita o destino na mesma operação."
        />
        <form className="flex flex-col gap-4" onSubmit={handleReview} noValidate>
          <TextField
            label="Id da conta de origem"
            value={origem}
            onChange={(event) => setOrigem(event.target.value.replace(/\D/g, ''))}
            error={errors.origem}
            inputMode="numeric"
            placeholder="Ex.: 1"
            autoComplete="off"
          />
          <TextField
            label="Id da conta de destino"
            value={destino}
            onChange={(event) => setDestino(event.target.value.replace(/\D/g, ''))}
            error={errors.destino}
            inputMode="numeric"
            placeholder="Ex.: 2"
            autoComplete="off"
          />
          <CurrencyInput
            label="Valor"
            value={valor}
            onChange={setValor}
            error={errors.valor}
            hint="Maior que zero; não pode exceder o saldo da origem."
          />

          {error ? <ApiErrorAlert error={error} /> : null}
          {data ? <TransacaoResult titulo="Transferência realizada" transacao={data} /> : null}

          <Button type="submit">Revisar transferência</Button>
        </form>
      </Card>

      <ConfirmModal
        open={confirming}
        title="Confirmar transferência"
        confirmLabel="Transferir"
        loading={status === 'loading'}
        onClose={() => {
          if (status !== 'loading') setConfirming(false)
        }}
        onConfirm={handleConfirm}
      >
        <p>
          Transferir{' '}
          <strong>{valorConfirmado !== null ? formatCurrency(valorConfirmado) : '—'}</strong> da conta{' '}
          <strong>#{origem || '—'}</strong> para a conta <strong>#{destino || '—'}</strong>?
        </p>
        <p className="mt-2 text-xs text-slate-500">
          A operação debita a conta de origem e credita a conta de destino.
        </p>
      </ConfirmModal>
    </div>
  )
}

export function TransferenciaPage() {
  return (
    <RequireRole role="ADMIN">
      <TransferenciaForm />
    </RequireRole>
  )
}
