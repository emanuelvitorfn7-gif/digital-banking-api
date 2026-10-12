const brlFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
})

export function formatCurrency(value: number | null | undefined): string {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return '—'
  }
  return brlFormatter.format(value)
}

/**
 * O Back-End serializa LocalDateTime em UTC, sem sufixo de fuso
 * (ex.: "2026-10-11T17:51:00"). Normalizamos para UTC antes de exibir.
 */
export function formatDateTime(iso: string | null | undefined, timeZone?: string): string {
  if (!iso) return '—'
  const normalized = /(?:z|[+-]\d{2}:?\d{2})$/i.test(iso) ? iso : `${iso}Z`
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) return iso
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
    timeZone,
  }).format(date)
}
