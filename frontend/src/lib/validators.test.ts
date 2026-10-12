import { describe, expect, it } from 'vitest'
import { isValidCpf, isValidEmail } from './validators'

describe('isValidCpf', () => {
  it('aceita CPF valido', () => {
    expect(isValidCpf('12345678909')).toBe(true)
    expect(isValidCpf('123.456.789-09')).toBe(true)
  })

  it('rejeita CPF com digito verificador incorreto', () => {
    expect(isValidCpf('12345678900')).toBe(false)
  })

  it('rejeita digitos repetidos e tamanho invalido', () => {
    expect(isValidCpf('11111111111')).toBe(false)
    expect(isValidCpf('123')).toBe(false)
  })
})

describe('isValidEmail', () => {
  it('aceita e-mail valido', () => {
    expect(isValidEmail('ana.silva@email.com')).toBe(true)
  })

  it('rejeita e-mail invalido', () => {
    expect(isValidEmail('ana.silva@email')).toBe(false)
    expect(isValidEmail('invalido')).toBe(false)
  })
})
