import { useState, type FormEvent } from 'react'
import { contasApi } from '../api/contas'
import { ApiError } from '../api/client'
import type { ContaResponse } from '../api/types'
import { Mensagem } from './Mensagem'

interface NovaContaFormProps {
  onCriada: (conta: ContaResponse) => void
  onCancelar: () => void
}

export function NovaContaForm({ onCriada, onCancelar }: NovaContaFormProps) {
  const [titular, setTitular] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      const conta = await contasApi.criar(titular.trim())
      setTitular('')
      onCriada(conta)
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Não foi possível criar a conta.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className="formulario" onSubmit={enviar}>
      {erro && <Mensagem tipo="erro" texto={erro} />}

      <label className="campo">
        <span>Nome do titular</span>
        <input
          className="input"
          value={titular}
          onChange={(evento) => setTitular(evento.target.value)}
          placeholder="Ex.: Ana Silva"
          maxLength={120}
          required
        />
      </label>

      <div className="acoes">
        <button type="submit" className="btn btn--primario" disabled={enviando}>
          {enviando ? 'Criando...' : 'Criar conta'}
        </button>
        <button type="button" className="btn btn--secundario" onClick={onCancelar} disabled={enviando}>
          Cancelar
        </button>
      </div>
    </form>
  )
}
