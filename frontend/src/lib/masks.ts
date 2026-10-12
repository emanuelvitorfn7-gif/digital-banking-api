export function onlyDigits(value: string): string {
  return value.replace(/\D+/g, '')
}

/** Aplica a mascara 000.000.000-00 progressivamente conforme o usuario digita. */
export function maskCpf(value: string): string {
  const digits = onlyDigits(value).slice(0, 11)
  const first = digits.slice(0, 3)
  const second = digits.slice(3, 6)
  const third = digits.slice(6, 9)
  const check = digits.slice(9, 11)

  let result = first
  if (second) result += `.${second}`
  if (third) result += `.${third}`
  if (check) result += `-${check}`
  return result
}

/** Mascara de moeda no estilo "centavos" (digitar 500 => "5,00"). */
export function maskCurrencyInput(value: string): string {
  const digits = onlyDigits(value).slice(0, 15)
  if (!digits) return ''
  const reais = Math.floor(Number(digits) / 100)
  const centavos = digits.slice(-2).padStart(2, '0')
  return `${new Intl.NumberFormat('pt-BR').format(reais)},${centavos}`
}

/** Converte o texto mascarado de volta para numero (500 => 5). */
export function parseCurrencyInput(value: string): number | null {
  const digits = onlyDigits(value)
  if (!digits) return null
  return Number(digits) / 100
}
