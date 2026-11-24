import { getToken } from '../auth/session'

/** Error de la API con el mensaje del servidor (RFC 9457) y errores por campo. */
export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

const BASE_URL = `${import.meta.env.VITE_API_URL ?? ''}/api`

interface ProblemDetail {
  detail?: string
  errors?: Record<string, string>
}

// La sesión la registra AuthProvider: si el servidor responde 401 con un token guardado,
// el token venció o ya no es válido y hay que volver al inicio de sesión.
let onUnauthorized: (() => void) | null = null
export function setUnauthorizedHandler(handler: (() => void) | null) {
  onUnauthorized = handler
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  // Con FormData el navegador pone el Content-Type (con el boundary) por su cuenta
  if (init.body !== undefined && !(init.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json')
  }
  const token = getToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)

  let response: Response
  try {
    response = await fetch(`${BASE_URL}${path}`, { ...init, headers })
  } catch {
    throw new ApiError(0, 'No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.')
  }

  if (!response.ok) {
    if (response.status === 401 && token) {
      onUnauthorized?.()
      throw new ApiError(401, 'Tu sesión venció. Inicia sesión de nuevo.')
    }
    let problem: ProblemDetail = {}
    try {
      problem = (await response.json()) as ProblemDetail
    } catch {
      // respuesta sin cuerpo JSON
    }
    throw new ApiError(response.status, problem.detail ?? `Error ${response.status}`, problem.errors)
  }

  // 204 y 202 no traen cuerpo
  const text = await response.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export const http = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) => request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  patch: <T>(path: string, body: unknown) => request<T>(path, { method: 'PATCH', body: JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}

export function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : 'Ocurrió un error inesperado'
}
