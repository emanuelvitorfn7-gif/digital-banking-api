import { onlyDigits } from './masks'

export function isValidCpf(value: string): boolean {
  const cpf = onlyDigits(value)
  if (cpf.length !== 11) return false
  if (/^(\d)\1{10}$/.test(cpf)) return false

  const digits = cpf.split('').map(Number)
  const calcCheckDigit = (length: number): number => {
    let sum = 0
    for (let index = 0; index < length; index += 1) {
      sum += digits[index] * (length + 1 - index)
    }
    const remainder = (sum * 10) % 11
    return remainder === 10 ? 0 : remainder
  }

  return calcCheckDigit(9) === digits[9] && calcCheckDigit(10) === digits[10]
}

export function isValidEmail(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim())
}
