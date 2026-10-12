import { useState, type FormEvent } from 'react'
import { clientesApi } from '../../api/endpoints'
import type { ClienteResponse } from '../../api/types'
import { RequireRole } from '../../components/layout/RequireRole'
import { Alert } from '../../components/ui/Alert'
import { ApiErrorAlert } from '../../components/ui/ApiErrorAlert'
import { Button } from '../../components/ui/Button'
import { Card, CardHeader } from '../../components/ui/Card'
import { LinkButton } from '../../components/ui/LinkButton'
import { TextField } from '../../components/ui/TextField'
import { useRequest } from '../../hooks/useRequest'
import { maskCpf, onlyDigits } from '../../lib/masks'
import { isValidCpf, isValidEmail } from '../../lib/validators'

interface FormErrors {
  nome?: string
  cpf?: string
  email?: string
}

function NovoClienteForm() {
  const [nome, setNome] = useState('')
  const [cpf, setCpf] = useState('')
  const [email, setEmail] = useState('')
  const [errors, setErrors] = useState<FormErrors>({})
  const { status, data, error, run, reset } = useRequest<ClienteResponse>()

  function validate(): boolean {
    const next: FormErrors = {}
    if (nome.trim().length === 0) next.nome = 'Informe o nome.'
    else if (nome.trim().length > 120) next.nome = 'Máximo de 120 caracteres.'
    if (!isValidCpf(cpf)) next.cpf = 'CPF inválido.'
    if (!isValidEmail(email)) next.email = 'E-mail inválido.'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!validate()) return
    await run(() =>
      clientesApi.criar({ nome: nome.trim(), cpf: onlyDigits(cpf), email: email.trim() }),
    )
  }

  function handleReset() {
    setNome('')
    setCpf('')
    setEmail('')
    setErrors({})
    reset()
  }

  return (
    <div className="mx-auto max-w-xl">
      <Card>
        <CardHeader
          title="Cadastrar cliente"
          description="Requer perfil ADMIN. CPF e e-mail precisam ser únicos."
        />
        <form className="flex flex-col gap-4" onSubmit={handleSubmit} noValidate>
          <TextField
            label="Nome completo"
            value={nome}
            onChange={(event) => setNome(event.target.value)}
            error={errors.nome}
            maxLength={120}
            autoComplete="off"
          />
          <TextField
            label="CPF"
            value={cpf}
            onChange={(event) => setCpf(maskCpf(event.target.value))}
            error={errors.cpf}
            inputMode="numeric"
            placeholder="000.000.000-00"
            autoComplete="off"
          />
          <TextField
            label="E-mail"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            error={errors.email}
            autoComplete="off"
          />

          {error ? <ApiErrorAlert error={error} /> : null}

          {data ? (
            <Alert variant="success" title="Cliente cadastrado">
              <ul className="space-y-0.5">
                <li>
                  Id: <strong>#{data.id}</strong>
                </li>
                <li>Nome: {data.nome}</li>
                <li>CPF: {data.cpf}</li>
                <li>E-mail: {data.email}</li>
              </ul>
              <p className="mt-2 text-xs">
                Os dados pessoais são exibidos já mascarados pela API.
              </p>
              <div className="mt-3 flex flex-wrap gap-2">
                <LinkButton to={`/contas/abrir?clienteId=${data.id}`} size="sm">
                  Abrir conta para este cliente
                </LinkButton>
                <LinkButton to={`/clientes/${data.id}`} size="sm" variant="secondary">
                  Ver cliente
                </LinkButton>
              </div>
            </Alert>
          ) : null}

          <div className="flex gap-3">
            <Button type="submit" loading={status === 'loading'}>
              Cadastrar
            </Button>
            {data ? (
              <Button variant="secondary" onClick={handleReset}>
                Cadastrar outro
              </Button>
            ) : null}
          </div>
        </form>
      </Card>
    </div>
  )
}

export function NovoClientePage() {
  return (
    <RequireRole role="ADMIN">
      <NovoClienteForm />
    </RequireRole>
  )
}
