// Tipos que reflejan los DTO del backend (Spring Boot).

export interface Owner {
  id: number
  email: string
  fullName: string
}

export interface AuthResponse {
  token: string
  expiresInSeconds: number
  owner: Owner
}

export interface LoginInput {
  email: string
  password: string
}

export interface RegisterInput extends LoginInput {
  fullName: string
}

export type ComplianceStatus = 'PENDIENTE' | 'CUMPLIDO' | 'PARCIAL' | 'DESCANSO'

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY'

export interface Driver {
  id: number
  fullName: string
  documentNumber: string
  phone: string | null
  active: boolean
}

export interface DriverInput {
  fullName: string
  documentNumber: string
  phone: string
  active: boolean
}

export interface Taxi {
  id: number
  plate: string
  model: string
  active: boolean
  driverId: number | null
  driverName: string | null
}

export interface TaxiInput {
  plate: string
  model: string
  driverId: number | null
  active: boolean
}

export interface DailyRecord {
  id: number
  version: number
  taxiId: number
  plate: string
  date: string // ISO yyyy-MM-dd
  dayOfWeek: DayOfWeek
  picoPlaca: boolean
  expectedProduction: number
  receivedProduction: number
  driverSavings: number
  extraExpenses: number
  netDelivered: number
  status: ComplianceStatus
  notes: string | null
}

export interface UpdateDailyRecordInput {
  version: number
  picoPlaca: boolean
  expectedProduction: number
  receivedProduction: number
  driverSavings: number
  extraExpenses: number
  status: ComplianceStatus | null // null = el servidor lo deriva
  notes: string | null
}

export interface CreateDayInput {
  date: string // ISO yyyy-MM-dd
  expectedProduction: number
}

export interface RecalculateResult {
  year: number
  updated: number
  total: number
}

export interface PicoPlacaInfo {
  date: string
  restrictedDigits: number[]
}

export interface PagedResponse<T> {
  content: T[]
  page: { size: number; number: number; totalElements: number; totalPages: number }
}

export interface GenerateYearResult {
  year: number
  created: number
  skipped: number
}

export interface Totals {
  expected: number
  received: number
  savings: number
  expenses: number
  net: number
}

export interface FleetSummary {
  from: string
  to: string
  activeTaxis: number
  activeDrivers: number
  totals: Totals
  complianceRate: number
}

export interface TaxiTotals {
  taxiId: number
  plate: string
  totals: Totals
}

export interface PeriodReport {
  label: string
  start: string
  end: string
  taxis: TaxiTotals[]
  totals: Totals
}
