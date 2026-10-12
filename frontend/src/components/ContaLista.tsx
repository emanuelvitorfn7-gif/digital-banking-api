import type { ContaResponse } from '../api/types'
import { formatarMoeda } from '../lib/formatters'

interface ContaListaProps {
  contas: ContaResponse[]
  onSelecionar: (conta: ContaResponse) => void
}

export function ContaLista({ contas, onSelecionar }: ContaListaProps) {
  if (contas.length === 0) {
    return <p className="vazio">Nenhuma conta cadastrada ainda.</p>
  }

  return (
    <ul className="lista-contas">
      {contas.map((conta) => (
        <li key={conta.id}>
          <button
            type="button"
            className="conta-item"
            onClick={() => onSelecionar(conta)}
          >
            <span className="conta-item__titular">{conta.titular}</span>
            <span className="conta-item__numero">Conta {conta.numero}</span>
            <span className="conta-item__saldo">{formatarMoeda(conta.saldo)}</span>
          </button>
        </li>
      ))}
    </ul>
  )
}
