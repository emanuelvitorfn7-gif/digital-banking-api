import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { setCredentials, type Credentials } from '../api/credentials'
import { verificarCredenciais } from '../api/endpoints'

export type Role = 'ADMIN' | 'CLIENTE'

export interface Session {
  username: string
  role: Role
}

interface AuthContextValue {
  session: Session | null
  isAuthenticated: boolean
  login: (credentials: Credentials, roleHint?: Role) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

/**
 * A API nao expoe o papel do usuario. Usamos o nome de usuario como PISTA de UI
 * (admin => ADMIN). A autorizacao real continua sendo aplicada pelo Back-End.
 */
export function derivarRole(username: string): Role {
  return username.trim().toLowerCase() === 'admin' ? 'ADMIN' : 'CLIENTE'
}

export function AuthProvider({
  children,
  initialSession = null,
}: {
  children: ReactNode
  initialSession?: Session | null
}) {
  const [session, setSession] = useState<Session | null>(initialSession)

  const login = useCallback(async (credentials: Credentials, roleHint?: Role) => {
    const valido = await verificarCredenciais(credentials)
    if (!valido) {
      throw new Error('Usuário ou senha inválidos.')
    }
    setCredentials(credentials)
    setSession({
      username: credentials.username.trim(),
      role: roleHint ?? derivarRole(credentials.username),
    })
  }, [])

  const logout = useCallback(() => {
    setCredentials(null)
    setSession(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ session, isAuthenticated: session !== null, login, logout }),
    [session, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de <AuthProvider>.')
  }
  return context
}
