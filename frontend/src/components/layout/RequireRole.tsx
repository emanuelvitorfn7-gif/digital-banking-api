import type { ReactNode } from 'react'
import { useAuth, type Role } from '../../auth/AuthContext'
import { Alert } from '../ui/Alert'

/**
 * Bloqueio de UI por perfil (PISTA). A autorizacao real e feita pela API:
 * mesmo contornando esta tela, o Back-End responderia HTTP 403.
 */
export function RequireRole({ role, children }: { role: Role; children: ReactNode }) {
  const { session } = useAuth()

  if (session?.role !== role) {
    return (
      <Alert variant="warning" title="Operação restrita">
        Seu perfil atual ({session?.role ?? 'desconhecido'}) não permite esta operação, que exige o
        perfil ADMIN. A autorização efetiva é aplicada pelo Back-End (a API responderia HTTP 403).
      </Alert>
    )
  }

  return <>{children}</>
}
