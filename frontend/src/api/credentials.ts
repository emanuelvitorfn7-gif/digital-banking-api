export interface Credentials {
  username: string
  password: string
}

let currentCredentials: Credentials | null = null

/**
 * Guarda as credenciais (HTTP Basic) APENAS em memoria, pelo tempo da sessao
 * do navegador. Nunca sao persistidas em localStorage/sessionStorage.
 */
export function setCredentials(credentials: Credentials | null): void {
  currentCredentials = credentials
}

export function getCredentials(): Credentials | null {
  return currentCredentials
}
