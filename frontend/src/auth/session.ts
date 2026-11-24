// El token de sesión vive en localStorage. Se envuelve en try/catch porque el navegador puede
// bloquear el almacenamiento (modo privado, datos de sitio bloqueados).
const TOKEN_KEY = 'taxi-fleet.token'

export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function saveToken(token: string) {
  try {
    localStorage.setItem(TOKEN_KEY, token)
  } catch {
    // sin almacenamiento: la sesión dura hasta recargar la página
  }
}

export function clearToken() {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    // nada que limpiar
  }
}
