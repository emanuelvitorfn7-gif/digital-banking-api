import { describe, expect, it } from 'vitest'
import { formatarMoeda } from './formatters'

describe('formatarMoeda', () => {
  it('formata um valor com o símbolo do real', () => {
    expect(formatarMoeda(10)).toContain('R$')
  })

  it('usa vírgula como separador decimal', () => {
    expect(formatarMoeda(1234.5)).toContain('1.234,50')
  })

  it('formata o zero com duas casas decimais', () => {
    expect(formatarMoeda(0)).toContain('0,00')
  })
})
