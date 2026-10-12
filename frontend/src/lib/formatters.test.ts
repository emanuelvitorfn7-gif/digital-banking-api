import { describe, expect, it } from 'vitest'
import { formatCurrency, formatDateTime } from './formatters'

describe('formatCurrency', () => {
  it('formata valores em Real brasileiro', () => {
    expect(formatCurrency(1234.5)).toContain('1.234,50')
    expect(formatCurrency(0)).toContain('0,00')
  })

  it('retorna travessão para valores ausentes', () => {
    expect(formatCurrency(null)).toBe('—')
    expect(formatDateTime(undefined)).toBe('—')
  })
})

describe('formatDateTime', () => {
  it('interpreta o valor como UTC e formata em pt-BR', () => {
    const resultado = formatDateTime('2026-10-11T17:51:00', 'UTC')
    expect(resultado).toContain('11/10/2026')
    expect(resultado).toContain('17:51')
  })

  it('aceita valor com fuso explicito', () => {
    expect(formatDateTime('2026-10-11T17:51:00Z', 'UTC')).toContain('11/10/2026')
  })
})
