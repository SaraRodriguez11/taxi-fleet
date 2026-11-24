import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  dailyApi,
  dashboardApi,
  driversApi,
  picoPlacaApi,
  reportsApi,
  taxisApi,
  type DailyListParams,
} from './resources'
import type { ComplianceStatus, CreateDayInput, DriverInput, TaxiInput, UpdateDailyRecordInput } from './types'

// Claves centralizadas: evitan strings sueltos y errores de invalidación.
export const keys = {
  taxis: (activeOnly: boolean) => ['taxis', activeOnly] as const,
  drivers: (activeOnly: boolean) => ['drivers', activeOnly] as const,
  daily: (params: DailyListParams) => ['daily', params] as const,
  summary: (year: number) => ['summary', year] as const,
  report: (kind: 'weekly' | 'monthly', year: number) => ['report', kind, year] as const,
}

export const useTaxis = (activeOnly = false) =>
  useQuery({ queryKey: keys.taxis(activeOnly), queryFn: () => taxisApi.list(activeOnly) })

export const useDrivers = (activeOnly = false) =>
  useQuery({ queryKey: keys.drivers(activeOnly), queryFn: () => driversApi.list(activeOnly) })

export const useDailyRecords = (params: DailyListParams | null) =>
  useQuery({
    queryKey: params ? keys.daily(params) : ['daily', 'disabled'],
    queryFn: () => dailyApi.list(params!),
    enabled: params !== null,
    placeholderData: keepPreviousData,
  })

export const useSummary = (year: number) =>
  useQuery({ queryKey: keys.summary(year), queryFn: () => dashboardApi.summary(year) })

export const useReport = (kind: 'weekly' | 'monthly', year: number) =>
  useQuery({ queryKey: keys.report(kind, year), queryFn: () => reportsApi[kind](year) })

/** Al cambiar datos contables se invalidan lecturas derivadas (dashboard y reportes). */
function useInvalidateAccounting() {
  const queryClient = useQueryClient()
  return () => {
    void queryClient.invalidateQueries({ queryKey: ['daily'] })
    void queryClient.invalidateQueries({ queryKey: ['summary'] })
    void queryClient.invalidateQueries({ queryKey: ['report'] })
  }
}

export function useSaveTaxi() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: number | null; input: TaxiInput }) =>
      id === null ? taxisApi.create(input) : taxisApi.update(id, input),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['taxis'] }),
  })
}

export function useDeactivateTaxi() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => taxisApi.deactivate(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['taxis'] }),
  })
}

export function useSaveDriver() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, input }: { id: number | null; input: DriverInput }) =>
      id === null ? driversApi.create(input) : driversApi.update(id, input),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['drivers'] })
      void queryClient.invalidateQueries({ queryKey: ['taxis'] })
    },
  })
}

export function useDeactivateDriver() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => driversApi.deactivate(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['drivers'] }),
  })
}

export function useUpdateDailyRecord() {
  const invalidate = useInvalidateAccounting()
  return useMutation({
    mutationFn: ({ id, input }: { id: number; input: UpdateDailyRecordInput }) => dailyApi.update(id, input),
    onSuccess: invalidate,
    // Ante un conflicto de edición (409) también se recarga para mostrar la versión vigente
    onError: invalidate,
  })
}

export function useChangeStatus() {
  const invalidate = useInvalidateAccounting()
  return useMutation({
    mutationFn: ({ id, version, status }: { id: number; version: number; status: ComplianceStatus }) =>
      dailyApi.changeStatus(id, version, status),
    onSuccess: invalidate,
    onError: invalidate,
  })
}

export function useDeleteDay() {
  const invalidate = useInvalidateAccounting()
  return useMutation({ mutationFn: (id: number) => dailyApi.remove(id), onSuccess: invalidate })
}

export function useCreateDay() {
  const invalidate = useInvalidateAccounting()
  return useMutation({
    mutationFn: ({ taxiId, input }: { taxiId: number; input: CreateDayInput }) => dailyApi.create(taxiId, input),
    onSuccess: invalidate,
  })
}

export function useRecalculatePicoPlaca() {
  const invalidate = useInvalidateAccounting()
  return useMutation({
    mutationFn: ({ taxiId, year, expected }: { taxiId: number; year: number; expected: number }) =>
      dailyApi.recalculatePicoPlaca(taxiId, year, expected),
    onSuccess: invalidate,
  })
}

export const usePicoPlaca = (date: string) =>
  useQuery({ queryKey: ['pico-placa', date] as const, queryFn: () => picoPlacaApi.byDate(date) })

export function useGenerateYear() {
  const invalidate = useInvalidateAccounting()
  return useMutation({
    mutationFn: ({ taxiId, year, expected }: { taxiId: number; year: number; expected: number }) =>
      dailyApi.generateYear(taxiId, year, expected),
    onSuccess: invalidate,
  })
}
