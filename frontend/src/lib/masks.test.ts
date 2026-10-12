import { describe, expect, it } from 'vitest'
import { maskCpf, maskCurrencyInput, onlyDigits, parseCurrencyInput } from './masks'

describe('onlyDigits', () => {
  it('remove caracteres nao numericos', () => {
    expect(onlyDigits('123.456.789-09')).toBe('12345678909')
  })
})

describe('maskCpf', () => {
  it('aplica a mascara progressivamente', () => {
    expect(maskCpf('123')).toBe('123')
    expect(maskCpf('123456')).toBe('123.456')
    expect(maskCpf('123456789')).toBe('123.456.789')
    expect(maskCpf('12345678909')).toBe('123.456.789-09')
  })

  it('ignora o excesso de digitos', () => {
    expect(maskCpf('123456789091234')).toBe('123.456.789-09')
  })
})

describe('maskCurrencyInput', () => {
  it('formata no estilo centavos', () => {
    expect(maskCurrencyInput('5')).toBe('0,05')
    expect(maskCurrencyInput('500')).toBe('5,00')
    expect(maskCurrencyInput('123456')).toBe('1.234,56')
  })

  it('retorna vazio quando nao ha digitos', () => {
    expect(maskCurrencyInput('')).toBe('')
  })
})

describe('parseCurrencyInput', () => {
  it('converte o texto mascarado em numero', () => {
    expect(parseCurrencyInput('5,00')).toBe(5)
    expect(parseCurrencyInput('1.234,56')).toBe(1234.56)
  })

  it('retorna null para valor vazio', () => {
    expect(parseCurrencyInput('')).toBeNull()
  })
})
