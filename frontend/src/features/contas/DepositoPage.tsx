import { useState, type FormEvent } from 'react'
import { useParams } from 'react-router-dom'
import { contasApi } from '../../api/endpoints'
import type { TransacaoResponse } from '../../api/types'
import { ContaIdPicker } from '../../components/ContaIdPicker'
import { TransacaoResult } from '../../components/data/TransacaoResult'
import { RequireRole } from '../../components/layout/RequireRole'
import { Alert } from '../../components/ui/Alert'
import { ApiErrorAlert } from '../../components/ui/ApiErrorAlert'
import { Button } from '../../components/ui/Button'
import { Card, CardHeader } from '../../components/ui/Card'
import { CurrencyInput } from '../../components/ui/CurrencyInput'
import { useRequest } from '../../hooks/useRequest'
import { parseCurrencyInput } from '../../lib/masks'

function DepositoForm() {
  const { id } = useParams()
  const [contaId, setContaId] = useState<number | null>(id && /^\d+$/.test(id) ? Number(id) : null)
  const [valor, setValor] = useState('')
  const [valorError, setValorError] = useState<string | null>(null)
  const [saldoAtualizado, setSaldoAtualizado] = useState<number | null>(null)
  const { status, data, error, run, reset } = useRequest<TransacaoResponse>()

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (contaId === null) return
    const parsed = parseCurrencyInput(valor)
    if (parsed === null || parsed <= 0) {
      setValorError('Informe um valor maior que zero.')
      return
    }
    setValorError(null)
    setSaldoAtualizado(null)
    const result = await run(() => contasApi.depositar(contaId, parsed))
    if (result) {
      setValor('')
      try {
        const conta = await contasApi.buscarPorId(contaId)
        setSaldoAtualizado(conta.saldo)
      } catch {
        setSaldoAtualizado(null)
      }
    }
  }

  if (contaId === null) {
    return (
      <div className="mx-auto max-w-xl">
        <Card>
          <CardHeader title="Depósito" description="Informe a conta de destino." />
          <ContaIdPicker onSelect={setContaId} />
        </Card>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-xl">
      <Card>
        <CardHeader title="Depósito" description={`Conta #${contaId}. Requer perfil ADMIN.`} />
        <form className="flex flex-col gap-4" onSubmit={handleSubmit} noValidate>
          <CurrencyInput
            label="Valor do depósito"
            value={valor}
            onChange={setValor}
            error={valorError}
            hint="Maior que zero e com até 2 casas decimais."
          />

          {error ? <ApiErrorAlert error={error} /> : null}
          {data ? (
            <TransacaoResult titulo="Depósito realizado" transacao={data} />
          ) : null}
          {saldoAtualizado !== null ? (
            <Alert variant="info" title="Saldo atualizado">
              Novo saldo da conta #{contaId}:{' '}
              <strong>
                {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(
                  saldoAtualizado,
                )}
              </strong>
            </Alert>
          ) : null}

          <div className="flex gap-3">
            <Button type="submit" loading={status === 'loading'}>
              Depositar
            </Button>
            <Button variant="secondary" onClick={() => { setContaId(null); reset(); setSaldoAtualizado(null) }}>
              Trocar conta
            </Button>
          </div>
        </form>
      </Card>
    </div>
  )
}

export function DepositoPage() {
  return (
    <RequireRole role="ADMIN">
      <DepositoForm />
    </RequireRole>
  )
}
