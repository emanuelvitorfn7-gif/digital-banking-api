import { useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { contasApi } from '../../api/endpoints'
import type { ContaResponse } from '../../api/types'
import { RequireRole } from '../../components/layout/RequireRole'
import { ContaCard } from '../../components/data/ContaCard'
import { Alert } from '../../components/ui/Alert'
import { ApiErrorAlert } from '../../components/ui/ApiErrorAlert'
import { Button } from '../../components/ui/Button'
import { Card, CardHeader } from '../../components/ui/Card'
import { LinkButton } from '../../components/ui/LinkButton'
import { TextField } from '../../components/ui/TextField'
import { useRequest } from '../../hooks/useRequest'

function AbrirContaForm() {
  const [searchParams] = useSearchParams()
  const [clienteId, setClienteId] = useState(searchParams.get('clienteId') ?? '')
  const [fieldError, setFieldError] = useState<string | null>(null)
  const { status, data, error, run, reset } = useRequest<ContaResponse>()

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const parsed = Number(clienteId)
    if (!Number.isInteger(parsed) || parsed <= 0) {
      setFieldError('Informe um id de cliente válido (número inteiro positivo).')
      return
    }
    setFieldError(null)
    await run(() => contasApi.abrir({ clienteId: parsed }))
  }

  return (
    <div className="mx-auto max-w-xl">
      <Card>
        <CardHeader
          title="Abrir conta"
          description="Requer perfil ADMIN. O número da conta é gerado pela API e o saldo inicial é zero."
        />
        <form className="flex flex-col gap-4" onSubmit={handleSubmit} noValidate>
          <TextField
            label="Id do cliente (titular)"
            value={clienteId}
            onChange={(event) => setClienteId(event.target.value.replace(/\D/g, ''))}
            error={fieldError}
            inputMode="numeric"
            placeholder="Ex.: 1"
            hint="A API não expõe listagem de clientes; informe o id manualmente ou cadastre um cliente."
            autoComplete="off"
          />

          {error ? <ApiErrorAlert error={error} /> : null}

          {data ? (
            <div className="flex flex-col gap-3">
              <Alert variant="success" title="Conta aberta">
                Guarde o número da conta para consultas futuras.
              </Alert>
              <ContaCard conta={data} />
              <div className="flex flex-wrap gap-2">
                <LinkButton to={`/contas/${data.id}/deposito`} size="sm">
                  Fazer um depósito
                </LinkButton>
                <LinkButton to={`/contas/${data.id}/extrato`} size="sm" variant="secondary">
                  Ver extrato
                </LinkButton>
              </div>
            </div>
          ) : null}

          <div className="flex gap-3">
            <Button type="submit" loading={status === 'loading'}>
              Abrir conta
            </Button>
            {data ? (
              <Button
                variant="secondary"
                onClick={() => {
                  reset()
                }}
              >
                Abrir outra
              </Button>
            ) : null}
          </div>
        </form>
      </Card>
    </div>
  )
}

export function AbrirContaPage() {
  return (
    <RequireRole role="ADMIN">
      <AbrirContaForm />
    </RequireRole>
  )
}
