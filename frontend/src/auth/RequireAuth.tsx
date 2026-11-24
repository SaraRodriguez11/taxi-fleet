import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { Loading } from '../components/ui'
import { useAuth } from './AuthContext'

/** Protege las rutas privadas: sin sesión se redirige al inicio de sesión. */
export default function RequireAuth() {
  const { status } = useAuth()
  const location = useLocation()

  if (status === 'loading') return <Loading label="Verificando sesión…" />
  if (status === 'anonymous') return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return <Outlet />
}
