import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { AppRoutes } from '../../App'
import { AuthProvider } from '../../auth/AuthContext'

function renderAt(path: string) {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <AppRoutes />
      </MemoryRouter>
    </AuthProvider>,
  )
}

describe('LoginPage', () => {
  it('mostra erro quando as credenciais sao invalidas (HTTP 401)', async () => {
    const user = userEvent.setup()
    renderAt('/login')

    await user.type(screen.getByLabelText('Usuário'), 'admin')
    await user.type(screen.getByLabelText('Senha'), 'senha-errada')
    await user.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByText(/Usuário ou senha inválidos/i)).toBeInTheDocument()
  })

  it('autentica e navega para o painel com credenciais validas', async () => {
    const user = userEvent.setup()
    renderAt('/login')

    await user.type(screen.getByLabelText('Usuário'), 'admin')
    await user.type(screen.getByLabelText('Senha'), 'admin123')
    await user.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByRole('heading', { name: 'Painel' })).toBeInTheDocument()
  })
})
