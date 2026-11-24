import type { InputHTMLAttributes, ReactNode } from 'react'
import type { ComplianceStatus } from '../api/types'
import { STATUS_LABELS } from '../lib/format'

export function PageHeader({ title, subtitle, actions }: { title: string; subtitle?: string; actions?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 className="text-3xl font-extrabold text-slate-900">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-slate-500">{subtitle}</p>}
      </div>
      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </div>
  )
}

export function Card({ children, className = '' }: { children: ReactNode; className?: string }) {
  return <div className={`rounded-2xl border border-slate-200/70 bg-white shadow-card ${className}`}>{children}</div>
}

export function StatCard({ label, value, hint }: { label: string; value: string; hint?: string }) {
  return (
    <Card className="p-4">
      <p className="text-sm font-medium text-slate-500">{label}</p>
      <p className="tabular mt-2 font-display text-3xl font-extrabold text-slate-900">{value}</p>
      {hint && <p className="mt-1 text-xs text-slate-500">{hint}</p>}
    </Card>
  )
}

type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost'

const VARIANTS: Record<ButtonVariant, string> = {
  primary: 'bg-amber-400 text-slate-900 shadow-[0_2px_0_var(--color-amber-600)] hover:bg-amber-300 active:translate-y-px active:shadow-none',
  secondary: 'border border-slate-300 bg-white text-slate-700 hover:bg-slate-50',
  danger: 'bg-red-600 text-white hover:bg-red-500',
  ghost: 'text-slate-600 hover:bg-slate-100',
}

export function Button({
  variant = 'primary',
  className = '',
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & { variant?: ButtonVariant }) {
  return (
    <button
      {...props}
      className={`rounded-xl px-3.5 py-2 text-sm font-semibold transition-all disabled:cursor-not-allowed disabled:opacity-50 ${VARIANTS[variant]} ${className}`}
    />
  )
}

export function Field({
  label,
  error,
  ...props
}: InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string }) {
  return (
    <label className="block text-sm">
      <span className="mb-1 block font-medium text-slate-700">{label}</span>
      <input
        {...props}
        className={`w-full rounded-xl border bg-white px-3.5 py-2.5 outline-none transition-shadow focus:ring-4 focus:ring-amber-300/50 ${
          error ? 'border-red-400' : 'border-slate-300'
        }`}
      />
      {error && <span className="mt-1 block text-xs text-red-600">{error}</span>}
    </label>
  )
}

const STATUS_STYLES: Record<ComplianceStatus, string> = {
  PENDIENTE: 'bg-red-100 text-red-800',
  CUMPLIDO: 'bg-green-100 text-green-800',
  PARCIAL: 'bg-amber-100 text-amber-800',
  DESCANSO: 'bg-sky-100 text-sky-800',
}

/** Color de fondo de la fila según el estado: rojo = pendiente, verde = entregado. */
export const STATUS_ROW_STYLES: Record<ComplianceStatus, string> = {
  PENDIENTE: 'bg-red-50',
  CUMPLIDO: 'bg-green-50',
  PARCIAL: 'bg-amber-50',
  DESCANSO: 'bg-sky-50',
}

const STATUS_ORDER = Object.keys(STATUS_LABELS) as ComplianceStatus[]

/** Selector de estado con el color del estado: un clic y se guarda, sin abrir el modo edición. */
export function StatusSelect({
  value,
  onChange,
  disabled,
}: {
  value: ComplianceStatus
  onChange: (status: ComplianceStatus) => void
  disabled?: boolean
}) {
  return (
    <select
      value={value}
      disabled={disabled}
      aria-label="Estado"
      onChange={(event) => onChange(event.target.value as ComplianceStatus)}
      className={`cursor-pointer rounded-full border-0 px-2.5 py-1 text-xs font-semibold outline-none focus:ring-2 focus:ring-amber-300 disabled:cursor-wait disabled:opacity-60 ${STATUS_STYLES[value]}`}
    >
      {STATUS_ORDER.map((status) => (
        <option key={status} value={status}>
          {STATUS_LABELS[status]}
        </option>
      ))}
    </select>
  )
}

export function ErrorBanner({ message }: { message: string }) {
  return (
    <div role="alert" className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {message}
    </div>
  )
}

export function Loading({ label = 'Cargando…' }: { label?: string }) {
  return <p className="py-8 text-center text-sm text-slate-500">{label}</p>
}

export function EmptyState({ children }: { children: ReactNode }) {
  return <p className="py-10 text-center text-sm text-slate-500">{children}</p>
}
