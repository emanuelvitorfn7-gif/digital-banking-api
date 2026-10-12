import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useState } from 'react'
import { describe, expect, it } from 'vitest'
import { CurrencyInput } from './CurrencyInput'

function Harness() {
  const [value, setValue] = useState('')
  return <CurrencyInput label="Valor" value={value} onChange={setValue} />
}

describe('CurrencyInput', () => {
  it('aplica a mascara de moeda enquanto digita', async () => {
    const user = userEvent.setup()
    render(<Harness />)

    const input = screen.getByLabelText('Valor')
    await user.type(input, '123456')

    expect(input).toHaveValue('1.234,56')
  })

  it('expoe o erro de forma acessivel', () => {
    render(<CurrencyInput label="Valor" value="" onChange={() => {}} error="Obrigatório" />)

    expect(screen.getByText('Obrigatório')).toBeInTheDocument()
    expect(screen.getByLabelText('Valor')).toHaveAttribute('aria-invalid', 'true')
  })
})
