import { useState, type FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { useAuth, type Role } from '../../auth/AuthContext'
import { Alert } from '../../components/ui/Alert'
import { Button } from '../../components/ui/Button'
import { TextField } from '../../components/ui/TextField'

export function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = (location.state as { from?: string } | null)?.from ?? '/'

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [roleHint, setRoleHint] = useState<Role | 'AUTO'>('AUTO')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={from} replace />
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!username.trim() || !password) {
      setError('Informe usuário e senha.')
      return
    }
    setError(null)
    setLoading(true)
    try {
      await login(
        { username: username.trim(), password },
        roleHint === 'AUTO' ? undefined : roleHint,
      )
      navigate(from, { replace: true })
    } catch (err) {
      setError(err instanceof ApiError || err instanceof Error ? err.message : 'Não foi possível entrar.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-brand-950 via-brand-800 to-brand-600 px-4 py-10">
      <div className="w-full max-w-md">
        <div className="mb-6 text-center text-white">
          <span className="mx-auto grid h-12 w-12 place-items-center rounded-2xl bg-white/10 text-2xl font-bold">
            Λ
          </span>
          <h1 className="mt-3 text-2xl font-semibold">Digital Banking</h1>
          <p className="text-sm text-brand-100">Interface de demonstração</p>
        </div>

        <div className="rounded-2xl bg-white p-6 shadow-xl">
          <h2 className="text-lg font-semibold text-slate-900">Entrar</h2>
          <p className="mt-1 text-sm text-slate-500">Autenticação HTTP Basic (demonstração).</p>

          <form className="mt-5 flex flex-col gap-4" onSubmit={handleSubmit} noValidate>
            <TextField
              label="Usuário"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              autoFocus
            />
            <TextField
              label="Senha"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
            />

            <div className="flex flex-col gap-1.5">
              <label htmlFor="role-hint" className="text-sm font-medium text-slate-700">
                Perfil (pista de interface)
              </label>
              <select
                id="role-hint"
                value={roleHint}
                onChange={(event) => setRoleHint(event.target.value as Role | 'AUTO')}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm shadow-sm focus:border-brand-500"
              >
                <option value="AUTO">Detectar pelo usuário</option>
                <option value="ADMIN">Administrador (pode escrever)</option>
                <option value="CLIENTE">Cliente (somente leitura)</option>
              </select>
            </div>

            {error ? (
              <Alert variant="error" title="Não foi possível entrar">
                {error}
              </Alert>
            ) : null}

            <Button type="submit" loading={loading} className="w-full">
              Entrar
            </Button>
          </form>

          <div className="mt-5 rounded-xl bg-slate-50 p-3 text-xs text-slate-500">
            <p className="font-medium text-slate-600">Contas de demonstração</p>
            <p className="mt-1">
              Usuários de exemplo: <strong>admin</strong> e <strong>cliente</strong>. As senhas são
              definidas na configuração do Back-End (veja o README) e nunca ficam no bundle deste
              Front-End.
            </p>
            <div className="mt-2 flex gap-2">
              <Button variant="ghost" size="sm" onClick={() => setUsername('admin')}>
                Usar &quot;admin&quot;
              </Button>
              <Button variant="ghost" size="sm" onClick={() => setUsername('cliente')}>
                Usar &quot;cliente&quot;
              </Button>
            </div>
          </div>
        </div>

        <p className="mt-4 text-center text-xs text-brand-100">
          As credenciais ficam apenas na memória do navegador e são perdidas ao recarregar a página.
        </p>
      </div>
    </div>
  )
}
