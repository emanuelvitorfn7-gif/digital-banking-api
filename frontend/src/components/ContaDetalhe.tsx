import { useState, type FormEvent } from 'react'
import { contasApi } from '../api/contas'
import { ApiError } from '../api/client'
import type { ContaResponse } from '../api/types'
import { formatarMoeda } from '../lib/formatters'
import { Mensagem } from './Mensagem'

type TipoOperacao = 'deposito' | 'saque'

interface ContaDetalheProps {
  conta: ContaResponse
  onVoltar: () => void
  onAtualizada: (conta: ContaResponse) => void
}

export function ContaDetalhe({ conta, onVoltar, onAtualizada }: ContaDetalheProps) {
  const [operacao, setOperacao] = useState<TipoOperacao | null>(null)
  const [valor, setValor] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [mensagem, setMensagem] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  function abrirOperacao(tipo: TipoOperacao) {
    setOperacao(tipo)
    setValor('')
    setMensagem(null)
    setErro(null)
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    if (!operacao) {
      return
    }

    setErro(null)
    setMensagem(null)

    const numero = Number(valor)
    if (!Number.isFinite(numero) || numero <= 0) {
      setErro('Informe um valor maior que zero.')
      return
    }

    setEnviando(true)
    try {
      const atualizada =
        operacao === 'deposito'
          ? await contasApi.depositar(conta.id, numero)
          : await contasApi.sacar(conta.id, numero)

      onAtualizada(atualizada)
      setMensagem(operacao === 'deposito' ? 'Depósito realizado com sucesso.' : 'Saque realizado com sucesso.')
      setOperacao(null)
      setValor('')
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Não foi possível concluir a operação.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="cartao">
      <button type="button" className="btn btn--texto" onClick={onVoltar}>
        ← Voltar
      </button>

      <h2 className="conta-titular">{conta.titular}</h2>
      <p className="conta-numero">Conta {conta.numero}</p>

      <div className="saldo">
        <span className="saldo__rotulo">Saldo disponível</span>
        <span className="saldo__valor">{formatarMoeda(conta.saldo)}</span>
      </div>

      {mensagem && <Mensagem tipo="sucesso" texto={mensagem} />}
      {erro && <Mensagem tipo="erro" texto={erro} />}

      {operacao === null ? (
        <div className="acoes">
          <button type="button" className="btn btn--primario" onClick={() => abrirOperacao('deposito')}>
            Depositar
          </button>
          <button type="button" className="btn btn--secundario" onClick={() => abrirOperacao('saque')}>
            Sacar
          </button>
        </div>
      ) : (
        <form className="formulario" onSubmit={enviar}>
          <label className="campo">
            <span>Valor do {operacao === 'deposito' ? 'depósito' : 'saque'}</span>
            <input
              className="input"
              type="number"
              min="0.01"
              step="0.01"
              value={valor}
              onChange={(evento) => setValor(evento.target.value)}
              placeholder="0,00"
              autoFocus
              required
            />
          </label>
          <div className="acoes">
            <button type="submit" className="btn btn--primario" disabled={enviando}>
              {enviando ? 'Enviando...' : 'Confirmar'}
            </button>
            <button
              type="button"
              className="btn btn--secundario"
              onClick={() => setOperacao(null)}
              disabled={enviando}
            >
              Cancelar
            </button>
          </div>
        </form>
      )}
    </section>
  )
}
