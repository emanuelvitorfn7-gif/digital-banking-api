import type { ReactNode } from 'react'

type Variant = 'info' | 'success' | 'warning' | 'error'

const VARIANT_CLASSES: Record<Variant, string> = {
  info: 'border-brand-200 bg-brand-50 text-brand-900',
  success: 'border-emerald-200 bg-emerald-50 text-emerald-900',
  warning: 'border-amber-200 bg-amber-50 text-amber-900',
  error: 'border-red-200 bg-red-50 text-red-900',
}

export function Alert({
  variant = 'info',
  title,
  children,
}: {
  variant?: Variant
  title?: ReactNode
  children?: ReactNode
}) {
  return (
    <div
      role={variant === 'error' ? 'alert' : 'status'}
      className={`rounded-xl border px-4 py-3 text-sm ${VARIANT_CLASSES[variant]}`}
    >
      {title ? <p className="font-semibold">{title}</p> : null}
      {children ? <div className={title ? 'mt-1' : ''}>{children}</div> : null}
    </div>
  )
}
