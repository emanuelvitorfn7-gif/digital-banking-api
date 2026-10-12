import { Card, CardHeader } from '../../components/ui/Card'

const ENDPOINTS = [
  ['POST', '/clientes', 'Cadastrar cliente (ADMIN)'],
  ['GET', '/clientes/{id}', 'Consultar cliente'],
  ['POST', '/contas', 'Abrir conta (ADMIN)'],
  ['GET', '/contas/{id}', 'Consultar conta e saldo'],
  ['POST', '/contas/{id}/depositos', 'Depositar (ADMIN)'],
  ['POST', '/contas/{id}/saques', 'Sacar (ADMIN)'],
  ['POST', '/transferencias', 'Transferir (ADMIN)'],
  ['GET', '/contas/{id}/extrato', 'Extrato paginado'],
]

export function SobrePage() {
  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-5">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Sobre esta interface</h1>
        <p className="mt-1 text-sm text-slate-500">
          Front-End de demonstração para a Digital Banking API. É um projeto de estudo.
        </p>
      </div>

      <Card>
        <CardHeader title="O que é" />
        <p className="text-sm text-slate-600">
          Interface web (React + TypeScript + Vite) que consome os endpoints reais do Back-End
          (Spring Boot + MySQL). Serve para exercitar o fluxo de cadastro, abertura de conta,
          depósito, saque, transferência e extrato.
        </p>
      </Card>

      <Card>
        <CardHeader title="Limitações de segurança" />
        <ul className="list-disc space-y-1 pl-5 text-sm text-slate-600">
          <li>A autenticação é HTTP Basic de demonstração; não há login, sessão ou JWT reais.</li>
          <li>
            As credenciais ficam apenas na memória do navegador e são descartadas ao recarregar a
            página — nunca são gravadas em localStorage ou sessionStorage.
          </li>
          <li>Não há senhas reais embutidas no bundle; o usuário digita as credenciais.</li>
          <li>O perfil exibido é apenas uma pista de interface; a API aplica a autorização real.</li>
          <li>Não é um banco real: não informe dados ou senhas pessoais verdadeiras.</li>
        </ul>
      </Card>

      <Card>
        <CardHeader title="Endpoints consumidos" />
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-3 py-2 font-medium">Método</th>
                <th className="px-3 py-2 font-medium">Rota</th>
                <th className="px-3 py-2 font-medium">Descrição</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {ENDPOINTS.map(([method, path, description]) => (
                <tr key={`${method} ${path}`}>
                  <td className="px-3 py-2 font-mono text-xs font-semibold text-brand-700">{method}</td>
                  <td className="px-3 py-2 font-mono text-xs text-slate-700">{path}</td>
                  <td className="px-3 py-2 text-slate-600">{description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p className="mt-3 text-xs text-slate-500">
          A API não expõe endpoint de login nem listagem de contas/clientes, por isso alguns fluxos
          pedem o id manualmente.
        </p>
      </Card>
    </div>
  )
}
