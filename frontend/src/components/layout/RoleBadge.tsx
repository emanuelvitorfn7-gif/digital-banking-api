import type { Role } from '../../auth/AuthContext'

export function RoleBadge({ role }: { role: Role }) {
  const isAdmin = role === 'ADMIN'
  return (
    <span
      className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
        isAdmin ? 'bg-brand-100 text-brand-800' : 'bg-slate-200 text-slate-700'
      }`}
    >
      {isAdmin ? 'Administrador' : 'Cliente'}
    </span>
  )
}
