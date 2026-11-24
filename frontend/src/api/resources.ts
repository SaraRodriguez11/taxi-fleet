import { http } from './client'
import type {
  AuthResponse,
  ComplianceStatus,
  CreateDayInput,
  DailyRecord,
  Driver,
  DriverInput,
  FleetSummary,
  GenerateYearResult,
  LoginInput,
  Owner,
  PagedResponse,
  PeriodReport,
  PicoPlacaInfo,
  RecalculateResult,
  RegisterInput,
  Taxi,
  TaxiInput,
  UpdateDailyRecordInput,
} from './types'

export const taxisApi = {
  list: (activeOnly = false) => http.get<Taxi[]>(`/taxis?activeOnly=${activeOnly}`),
  create: (input: TaxiInput) => http.post<Taxi>('/taxis', input),
  update: (id: number, input: TaxiInput) => http.put<Taxi>(`/taxis/${id}`, input),
  deactivate: (id: number) => http.delete<void>(`/taxis/${id}`),
}

export const driversApi = {
  list: (activeOnly = false) => http.get<Driver[]>(`/drivers?activeOnly=${activeOnly}`),
  create: (input: DriverInput) => http.post<Driver>('/drivers', input),
  update: (id: number, input: DriverInput) => http.put<Driver>(`/drivers/${id}`, input),
  deactivate: (id: number) => http.delete<void>(`/drivers/${id}`),
}

export interface DailyListParams {
  taxiId: number
  year: number
  month: number | null // null = año completo
  page: number
  size: number
}

export const dailyApi = {
  list: ({ taxiId, year, month, page, size }: DailyListParams) => {
    const query = new URLSearchParams({ year: String(year), page: String(page), size: String(size) })
    if (month !== null) query.set('month', String(month))
    return http.get<PagedResponse<DailyRecord>>(`/taxis/${taxiId}/daily-records?${query}`)
  },
  generateYear: (taxiId: number, year: number, expectedProduction: number) =>
    http.post<GenerateYearResult>(
      `/taxis/${taxiId}/daily-records/generate?year=${year}&expectedProduction=${expectedProduction}`,
    ),
  update: (id: number, input: UpdateDailyRecordInput) => http.put<DailyRecord>(`/daily-records/${id}`, input),
  create: (taxiId: number, input: CreateDayInput) =>
    http.post<DailyRecord>(`/taxis/${taxiId}/daily-records`, input),
  changeStatus: (id: number, version: number, status: ComplianceStatus) =>
    http.patch<DailyRecord>(`/daily-records/${id}/status`, { version, status }),
  remove: (id: number) => http.delete<void>(`/daily-records/${id}`),
  recalculatePicoPlaca: (taxiId: number, year: number, expectedProduction: number) =>
    http.post<RecalculateResult>(
      `/taxis/${taxiId}/daily-records/recalculate-pico-placa?year=${year}&expectedProduction=${expectedProduction}`,
    ),
}

export const picoPlacaApi = {
  byDate: (date: string) => http.get<PicoPlacaInfo>(`/pico-placa?date=${date}`),
}

export const dashboardApi = {
  summary: (year: number) => http.get<FleetSummary>(`/dashboard/summary?from=${year}-01-01&to=${year}-12-31`),
}

export const reportsApi = {
  weekly: (year: number) => http.get<PeriodReport[]>(`/reports/weekly?year=${year}`),
  monthly: (year: number) => http.get<PeriodReport[]>(`/reports/monthly?year=${year}`),
}

export const authApi = {
  login: (input: LoginInput) => http.post<AuthResponse>('/auth/login', input),
  register: (input: RegisterInput) => http.post<AuthResponse>('/auth/register', input),
  me: () => http.get<Owner>('/auth/me'),
}
