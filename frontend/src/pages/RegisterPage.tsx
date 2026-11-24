import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { ApiError, errorMessage } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import AuthShell from '../components/AuthShell'
import { Button, ErrorBanner, Field } from '../components/ui'

export default function RegisterPage() {
  const { status, register } = useAuth()
  const navigate = useNavigate()

  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState<unknown>(null)
  const [pending, setPending] = useState(false)

  if (status === 'authenticated') return <Navigate to="/" replace />

  const mismatch = confirm !== '' && confirm !== password

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (mismatch) return
    setPending(true)
    setError(null)
    try {
      await register({ fullName, email, password })
      navigate('/', { replace: true })
    } catch (err) {
      setError(err)
    } finally {
      setPending(false)
    }
  }

  const fieldErrors = error instanceof ApiError ? error.fieldErrors : {}

  return (
    <AuthShell
      title="Crear cuenta"
      subtitle="Registra tu cuenta de propietario para administrar tus taxis."
      footer={
        <>
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="font-semibold text-amber-600 hover:underline">
            Inicia sesión
          </Link>
        </>
      }
    >
      <form onSubmit={submit} className="space-y-4">
        <Field
          label="Nombre completo"
          autoComplete="name"
          value={fullName}
          error={fieldErrors.fullName}
          onChange={(e) => setFullName(e.target.value)}
          required
        />
        <Field
          label="Correo electrónico"
          type="email"
          autoComplete="email"
          value={email}
          error={fieldErrors.email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <Field
          label="Contraseña (mínimo 8 caracteres)"
          type="password"
          autoComplete="new-password"
          minLength={8}
          maxLength={72}
          value={password}
          error={fieldErrors.password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Field
          label="Repite la contraseña"
          type="password"
          autoComplete="new-password"
          value={confirm}
          error={mismatch ? 'Las contraseñas no coinciden' : undefined}
          onChange={(e) => setConfirm(e.target.value)}
          required
        />
        {error !== null && Object.keys(fieldErrors).length === 0 && <ErrorBanner message={errorMessage(error)} />}
        <Button type="submit" disabled={pending || mismatch} className="w-full">
          {pending ? 'Creando cuenta…' : 'Crear cuenta'}
        </Button>
      </form>
    </AuthShell>
  )
}
