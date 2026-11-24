import type { ComplianceStatus, DayOfWeek } from '../api/types'

const currency = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
})

// yyyy-MM-dd en hora local (toISOString devolvería la fecha UTC y puede quedar un día adelante)
export const TODAY = new Date().toLocaleDateString('en-CA')

export const formatCOP = (value: number) => currency.format(value)

export const formatPercent = (value: number) => `${value.toFixed(1)} %`

const DAY_NAMES: Record<DayOfWeek, string> = {
  MONDAY: 'Lunes',
  TUESDAY: 'Martes',
  WEDNESDAY: 'Miércoles',
  THURSDAY: 'Jueves',
  FRIDAY: 'Viernes',
  SATURDAY: 'Sábado',
  SUNDAY: 'Domingo',
}

export const dayName = (day: DayOfWeek) => DAY_NAMES[day]

export const MONTH_NAMES = [
  'Enero',
  'Febrero',
  'Marzo',
  'Abril',
  'Mayo',
  'Junio',
  'Julio',
  'Agosto',
  'Septiembre',
  'Octubre',
  'Noviembre',
  'Diciembre',
]

/** yyyy-MM-dd -> dd/MM/yyyy sin pasar por Date (evita desfases de zona horaria). */
export function formatDate(iso: string) {
  const [year, month, day] = iso.split('-')
  return `${day}/${month}/${year}`
}

export const STATUS_LABELS: Record<ComplianceStatus, string> = {
  PENDIENTE: 'Pendiente',
  CUMPLIDO: 'Entregado',
  PARCIAL: 'Parcial',
  DESCANSO: 'Descanso',
}
