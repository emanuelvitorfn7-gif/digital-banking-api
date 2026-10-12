import { useState, type FormEvent } from 'react'
import { Button } from './ui/Button'
import { TextField } from './ui/TextField'

export function ContaIdPicker({ onSelect }: { onSelect: (id: number) => void }) {
  const [value, setValue] = useState('')
  const [error, setError] = useState<string | null>(null)

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    const parsed = Number(value)
    if (!Number.isInteger(parsed) || parsed <= 0) {
      setError('Informe um id de conta válido (número inteiro positivo).')
      return
    }
    setError(null)
    onSelect(parsed)
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-3 sm:flex-row sm:items-end">
      <div className="w-full sm:max-w-xs">
        <TextField
          label="Id da conta"
          inputMode="numeric"
          autoComplete="off"
          value={value}
          onChange={(event) => setValue(event.target.value.replace(/\D/g, ''))}
          error={error}
          hint="A API não expõe listagem de contas; informe o id manualmente."
          placeholder="Ex.: 1"
        />
      </div>
      <Button type="submit">Continuar</Button>
    </form>
  )
}
