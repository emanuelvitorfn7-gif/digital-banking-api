import { useCallback, useEffect, useState } from 'react'
import { ApiError } from './api/client'
import { contasApi } from './api/contas'
import type { ContaResponse } from './api/types'
import { ContaDetalhe } from './components/ContaDetalhe'
import { ContaLista } from './components/ContaLista'
import { Mensagem } from './components/Mensagem'
import { NovaContaForm } from './components/NovaContaForm'

export function App() {
  const [contas, setContas] = useState<ContaResponse[]>([])
  const [selecionada, setSelecionada] = useState<ContaResponse | null>(null)
  const [criando, setCriando] = useState(false)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  const carregar = useCallback(async () => {
    setCarregando(true)
    setErro(null)
    try {
      setContas(await contasApi.listar())
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Não foi possível carregar as contas.')
    } finally {
      setCarregando(false)
    }
  }, [])

  useEffect(() => {
    void carregar()
  }, [carregar])

  function aoCriar(conta: ContaResponse) {
    setContas((atual) => [...atual, conta])
    setCriando(false)
    setSelecionada(conta)
  }

  function aoAtualizar(conta: ContaResponse) {
    setSelecionada(conta)
    setContas((atual) => atual.map((item) => (item.id === conta.id ? conta : item)))
  }

  return (
    <div className="app">
      <header className="topo">
        <h1 className="topo__titulo">Digital Banking</h1>
        <p className="topo__subtitulo">Contas, depósitos e saques</p>
      </header>

      <main className="conteudo">
        {selecionada ? (
          <ContaDetalhe
            conta={selecionada}
            onVoltar={() => setSelecionada(null)}
            onAtualizada={aoAtualizar}
          />
        ) : (
          <section className="cartao">
            <div className="cartao__cabecalho">
              <h2>Contas cadastradas</h2>
              {!criando && (
                <button type="button" className="btn btn--primario" onClick={() => setCriando(true)}>
                  Nova conta
                </button>
              )}
            </div>

            {criando && <NovaContaForm onCriada={aoCriar} onCancelar={() => setCriando(false)} />}
            {erro && <Mensagem tipo="erro" texto={erro} />}

            {carregando ? (
              <p className="vazio">Carregando...</p>
            ) : (
              <ContaLista contas={contas} onSelecionar={(conta) => setSelecionada(conta)} />
            )}
          </section>
        )}
      </main>
    </div>
  )
}
