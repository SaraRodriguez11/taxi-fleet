import { useQueryClient } from '@tanstack/react-query'
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { setUnauthorizedHandler } from '../api/client'
import { authApi } from '../api/resources'
import type { LoginInput, Owner, RegisterInput } from '../api/types'
import { clearToken, getToken, saveToken } from './session'

type Status = 'loading' | 'authenticated' | 'anonymous'

interface AuthState {
  status: Status
  owner: Owner | null
  login: (input: LoginInput) => Promise<void>
  register: (input: RegisterInput) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()
  const [owner, setOwner] = useState<Owner | null>(null)
  // Si hay un token guardado, se valida con el servidor antes de mostrar nada
  const [checking, setChecking] = useState(() => getToken() !== null)

  const logout = useCallback(() => {
    clearToken()
    setOwner(null)
    queryClient.clear() // nunca se deben quedar datos de un propietario en caché para el siguiente
  }, [queryClient])

  useEffect(() => {
    setUnauthorizedHandler(logout)
    return () => setUnauthorizedHandler(null)
  }, [logout])

  useEffect(() => {
    if (!getToken()) return
    let cancelled = false
    authApi
      .me()
      .then((me) => {
        if (!cancelled) setOwner(me)
      })
      .catch(() => {
        if (!cancelled) clearToken()
      })
      .finally(() => {
        if (!cancelled) setChecking(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback(
    async (input: LoginInput) => {
      const response = await authApi.login(input)
      queryClient.clear()
      saveToken(response.token)
      setOwner(response.owner)
    },
    [queryClient],
  )

  const register = useCallback(
    async (input: RegisterInput) => {
      const response = await authApi.register(input)
      queryClient.clear()
      saveToken(response.token)
      setOwner(response.owner)
    },
    [queryClient],
  )

  const value = useMemo<AuthState>(
    () => ({
      status: checking ? 'loading' : owner ? 'authenticated' : 'anonymous',
      owner,
      login,
      register,
      logout,
    }),
    [checking, owner, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return context
}
