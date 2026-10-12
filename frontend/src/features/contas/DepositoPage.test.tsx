import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { AuthProvider } from '../../auth/AuthContext'
import { DepositoPage } from './DepositoPage'

function renderPage() {
  return render(
    <AuthProvider initialSession={{ username: 'admin', role: 'ADMIN' }}>
      <MemoryRouter initialEntries={['/contas/1/deposito']}>
        <Routes>
          <Route path="/contas/:id/deposito" element={<DepositoPage />} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

describe('DepositoPage', () => {
  it('realiza um deposito e exibe o resultado com o saldo atualizado', async () => {
    const user = userEvent.setup()
    renderPage()

    const input = screen.getByLabelText('Valor do depósito')
    await user.type(input, '5000')
    expect(input).toHaveValue('50,00')

    await user.click(screen.getByRole('button', { name: 'Depositar' }))

    expect(await screen.findByText(/Depósito realizado/i)).toBeInTheDocument()
    expect(await screen.findByText(/Novo saldo da conta #1/i)).toBeInTheDocument()
  })

  it('valida o valor antes de enviar', async () => {
    const user = userEvent.setup()
    renderPage()

    await user.click(screen.getByRole('button', { name: 'Depositar' }))

    expect(await screen.findByText(/maior que zero/i)).toBeInTheDocument()
  })
})
