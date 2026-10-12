import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { AppShell } from './components/layout/AppShell'
import { RequireAuth } from './components/layout/RequireAuth'
import { EmptyState } from './components/ui/EmptyState'
import { LinkButton } from './components/ui/LinkButton'
import { LoginPage } from './features/auth/LoginPage'
import { ClienteDetalhePage } from './features/clientes/ClienteDetalhePage'
import { NovoClientePage } from './features/clientes/NovoClientePage'
import { AbrirContaPage } from './features/contas/AbrirContaPage'
import { DepositoPage } from './features/contas/DepositoPage'
import { SaquePage } from './features/contas/SaquePage'
import { DashboardPage } from './features/dashboard/DashboardPage'
import { ExtratoPage } from './features/extrato/ExtratoPage'
import { SobrePage } from './features/sobre/SobrePage'
import { TransferenciaPage } from './features/transferencias/TransferenciaPage'

function NotFoundPage() {
  return (
    <div className="mx-auto max-w-lg py-10">
      <EmptyState title="Página não encontrada" description="O endereço acessado não existe." />
      <div className="mt-4 flex justify-center">
        <LinkButton to="/" variant="secondary">
          Voltar ao painel
        </LinkButton>
      </div>
    </div>
  )
}

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppShell />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/contas/abrir" element={<AbrirContaPage />} />
          <Route path="/contas/:id/deposito" element={<DepositoPage />} />
          <Route path="/contas/:id/saque" element={<SaquePage />} />
          <Route path="/contas/:id/extrato" element={<ExtratoPage />} />
          <Route path="/clientes/novo" element={<NovoClientePage />} />
          <Route path="/clientes/:id" element={<ClienteDetalhePage />} />
          <Route path="/transferencias" element={<TransferenciaPage />} />
          <Route path="/sobre" element={<SobrePage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  )
}

export function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  )
}
