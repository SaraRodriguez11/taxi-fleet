import { useState, type KeyboardEvent } from 'react'
import { errorMessage } from '../api/client'
import {
  useChangeStatus,
  useCreateDay,
  useDailyRecords,
  useDeleteDay,
  useGenerateYear,
  usePicoPlaca,
  useRecalculatePicoPlaca,
  useTaxis,
  useUpdateDailyRecord,
} from '../api/hooks'
import type { ComplianceStatus, DailyRecord } from '../api/types'
import { Button, Card, EmptyState, ErrorBanner, Loading, PageHeader, STATUS_ROW_STYLES, StatusSelect } from '../components/ui'
import { dayName, formatCOP, formatDate, MONTH_NAMES, STATUS_LABELS, TODAY } from '../lib/format'
import { YearInput } from './DashboardPage'

const PAGE_SIZE = 50
const now = new Date()
const DEFAULT_EXPECTED = 120000

export default function DailyControlPage() {
  const taxis = useTaxis(true)
  const [selectedTaxiId, setTaxiId] = useState<number | null>(null)
  const [year, setYear] = useState(now.getFullYear())
  const [month, setMonth] = useState<number | null>(now.getMonth() + 1)
  const [page, setPage] = useState(0)

  // Mientras no se elija uno, se usa el primer taxi (valor derivado, sin efectos)
  const taxiId = selectedTaxiId ?? taxis.data?.[0]?.id ?? null

  const records = useDailyRecords(taxiId === null ? null : { taxiId, year, month, page, size: PAGE_SIZE })

  const resetPage = <T,>(setter: (value: T) => void) => (value: T) => {
    setter(value)
    setPage(0)
  }

  return (
    <>
      <PageHeader title="Control diario" subtitle="Producción, ahorro y gastos de cada taxi por día" />

      <TodayPicoPlaca />

      {taxis.isError && <ErrorBanner message={errorMessage(taxis.error)} />}
      {taxis.data?.length === 0 && (
        <Card>
          <EmptyState>Primero registra al menos un taxi activo en la sección “Taxis”.</EmptyState>
        </Card>
      )}

      {taxiId !== null && taxis.data && (
        <>
          <Card className="mb-4 flex flex-wrap items-end gap-4 p-4">
            <label className="text-sm">
              <span className="mb-1 block font-medium text-slate-700">Taxi</span>
              <select
                value={taxiId}
                onChange={(e) => resetPage(setTaxiId)(Number(e.target.value))}
                className="rounded-lg border border-slate-300 bg-white px-3 py-2"
              >
                {taxis.data.map((taxi) => (
                  <option key={taxi.id} value={taxi.id}>
                    {taxi.plate} · {taxi.model}
                  </option>
                ))}
              </select>
            </label>
            <YearInput value={year} onChange={resetPage(setYear)} />
            <label className="text-sm">
              <span className="mb-1 block font-medium text-slate-700">Mes</span>
              <select
                value={month ?? ''}
                onChange={(e) => resetPage(setMonth)(e.target.value ? Number(e.target.value) : null)}
                className="rounded-lg border border-slate-300 bg-white px-3 py-2"
              >
                <option value="">Todo el año</option>
                {MONTH_NAMES.map((name, index) => (
                  <option key={name} value={index + 1}>
                    {name}
                  </option>
                ))}
              </select>
            </label>
          </Card>

          <ToolsPanel taxiId={taxiId} year={year} />

          {records.isPending && <Loading />}
          {records.isError && <ErrorBanner message={errorMessage(records.error)} />}

          {records.data && records.data.content.length === 0 && (
            <Card>
              <EmptyState>
                No hay registros para este periodo. Usa “Generar control del año” o agrega un día.
              </EmptyState>
            </Card>
          )}

          {records.data && records.data.content.length > 0 && (
            <Card className="overflow-x-auto">
              <table className="tabular w-full text-sm">
                <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                  <tr>
                    <th className="px-3 py-3">Fecha</th>
                    <th className="px-3 py-3">Día</th>
                    <th className="px-3 py-3">Pico y placa</th>
                    <th className="px-3 py-3 text-right">Esperada</th>
                    <th className="px-3 py-3 text-right">Recibida</th>
                    <th className="px-3 py-3 text-right">Ahorro</th>
                    <th className="px-3 py-3 text-right">Gastos extra</th>
                    <th className="px-3 py-3 text-right">Neto</th>
                    <th className="px-3 py-3">Estado</th>
                    <th className="px-3 py-3" />
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {records.data.content.map((record) => (
                    <DailyRow key={`${record.id}-${record.version}`} record={record} />
                  ))}
                </tbody>
              </table>
              <Pagination
                page={records.data.page.number}
                totalPages={records.data.page.totalPages}
                totalElements={records.data.page.totalElements}
                onChange={setPage}
              />
            </Card>
          )}
        </>
      )}
    </>
  )
}

/** Franja informativa: qué placas no circulan hoy (sirve para contrastar con la fuente oficial). */
function TodayPicoPlaca() {
  const today = usePicoPlaca(TODAY)
  if (!today.data) return null
  const digits = today.data.restrictedDigits
  return (
    <p className="mb-4 rounded-lg border border-sky-200 bg-sky-50 px-4 py-2 text-sm text-sky-900">
      {digits.length === 0 ? (
        <>Hoy no hay Pico y Placa para taxis.</>
      ) : (
        <>
          Hoy no circulan los taxis con placa terminada en{' '}
          <strong>{digits.join(' y ')}</strong>.
        </>
      )}
    </p>
  )
}

function ToolsPanel({ taxiId, year }: { taxiId: number; year: number }) {
  const [expected, setExpected] = useState(DEFAULT_EXPECTED)
  const [newDate, setNewDate] = useState(TODAY)
  const generate = useGenerateYear()
  const recalculate = useRecalculatePicoPlaca()
  const create = useCreateDay()

  const lastError = [generate.error, recalculate.error, create.error].find(Boolean)

  return (
    <Card className="mb-4 p-4">
      <div className="flex flex-wrap items-end gap-3">
        <label className="text-sm">
          <span className="mb-1 block font-medium text-slate-700">Producción diaria esperada</span>
          <input
            type="number"
            min={0}
            step={1000}
            value={expected}
            onChange={(e) => setExpected(Number(e.target.value))}
            className="w-40 rounded-lg border border-slate-300 px-3 py-2"
          />
        </label>
        <Button
          variant="secondary"
          disabled={generate.isPending || expected < 0}
          onClick={() => generate.mutate({ taxiId, year, expected })}
        >
          {generate.isPending ? 'Generando…' : `Generar control de ${year}`}
        </Button>
        <Button
          variant="secondary"
          disabled={recalculate.isPending || expected < 0}
          onClick={() => recalculate.mutate({ taxiId, year, expected })}
          title="Vuelve a calcular qué días son Pico y Placa con la regla vigente"
        >
          {recalculate.isPending ? 'Actualizando…' : 'Actualizar Pico y Placa'}
        </Button>

        <span className="mx-1 hidden h-9 w-px bg-slate-200 sm:block" />

        <label className="text-sm">
          <span className="mb-1 block font-medium text-slate-700">Agregar un día</span>
          <input
            type="date"
            value={newDate}
            onChange={(e) => setNewDate(e.target.value)}
            className="rounded-lg border border-slate-300 px-3 py-2"
          />
        </label>
        <Button
          disabled={create.isPending || !newDate || expected < 0}
          onClick={() => create.mutate({ taxiId, input: { date: newDate, expectedProduction: expected } })}
        >
          {create.isPending ? 'Agregando…' : 'Agregar día'}
        </Button>
      </div>

      <p className="mt-3 text-xs text-slate-500">
        “Generar” crea los días que falten del año sin duplicar los existentes. “Actualizar Pico y Placa” corrige las
        marcas de los días ya creados; los días con dinero registrado conservan sus montos.
      </p>

      {generate.isSuccess && (
        <p className="mt-2 text-sm text-emerald-700">
          {generate.data.created} días creados, {generate.data.skipped} ya existían.
        </p>
      )}
      {recalculate.isSuccess && (
        <p className="mt-2 text-sm text-emerald-700">
          {recalculate.data.updated} de {recalculate.data.total} días cambiaron su marca de Pico y Placa.
        </p>
      )}
      {lastError && (
        <div className="mt-2">
          <ErrorBanner message={errorMessage(lastError)} />
        </div>
      )}
    </Card>
  )
}

const STATUS_OPTIONS = Object.keys(STATUS_LABELS) as ComplianceStatus[]

function DailyRow({ record }: { record: DailyRecord }) {
  const [editing, setEditing] = useState(false)
  const update = useUpdateDailyRecord()
  const changeStatus = useChangeStatus()
  const remove = useDeleteDay()

  const [draft, setDraft] = useState({
    picoPlaca: record.picoPlaca,
    expected: record.expectedProduction,
    received: record.receivedProduction,
    savings: record.driverSavings,
    expenses: record.extraExpenses,
    status: null as ComplianceStatus | null,
  })

  // Vista previa del neto mientras se edita (el valor oficial lo calcula el servidor)
  const previewNet = draft.received - draft.savings - draft.expenses
  const invalid = previewNet < 0 || [draft.expected, draft.received, draft.savings, draft.expenses].some((v) => v < 0)
  const rowError = [update.error, changeStatus.error, remove.error].find(Boolean)

  const save = () =>
    update.mutate(
      {
        id: record.id,
        input: {
          version: record.version,
          picoPlaca: draft.picoPlaca,
          expectedProduction: draft.expected,
          receivedProduction: draft.received,
          driverSavings: draft.savings,
          extraExpenses: draft.expenses,
          status: draft.status,
          notes: record.notes,
        },
      },
      { onSuccess: () => setEditing(false) },
    )

  const confirmDelete = () => {
    if (confirm(`¿Eliminar el registro del ${formatDate(record.date)} (${record.plate})? Esta acción no se puede deshacer.`)) {
      remove.mutate(record.id)
    }
  }

  // Enter guarda y Escape cancela, sin tener que buscar los botones
  const onKeyDown = (event: KeyboardEvent) => {
    if (event.key === 'Enter' && !invalid && !update.isPending) save()
    if (event.key === 'Escape') setEditing(false)
  }

  const numberCell = (key: 'expected' | 'received' | 'savings' | 'expenses') => (
    <input
      type="number"
      min={0}
      step={500}
      value={draft[key]}
      onChange={(e) => setDraft({ ...draft, [key]: Number(e.target.value) })}
      onKeyDown={onKeyDown}
      className="w-28 rounded border border-slate-300 px-2 py-1 text-right"
      aria-label={key}
    />
  )

  const errorRow = (rowError || previewNet < 0) && (
    <tr className={editing ? 'bg-amber-50' : ''}>
      <td colSpan={10} className="px-3 pb-3">
        <ErrorBanner
          message={
            editing && previewNet < 0
              ? 'El ahorro y los gastos extra no pueden superar la producción recibida'
              : errorMessage(rowError)
          }
        />
      </td>
    </tr>
  )

  if (!editing) {
    return (
      <>
        <tr className={STATUS_ROW_STYLES[record.status]}>
          <td className="px-3 py-2 whitespace-nowrap">{formatDate(record.date)}</td>
          <td className="px-3 py-2">{dayName(record.dayOfWeek)}</td>
          <td className="px-3 py-2">
            {record.picoPlaca ? (
              <span className="font-semibold text-sky-700">Aplica</span>
            ) : (
              <span className="text-slate-400">No</span>
            )}
          </td>
          <td className="px-3 py-2 text-right">{formatCOP(record.expectedProduction)}</td>
          <td className="px-3 py-2 text-right">{formatCOP(record.receivedProduction)}</td>
          <td className="px-3 py-2 text-right">{formatCOP(record.driverSavings)}</td>
          <td className="px-3 py-2 text-right">{formatCOP(record.extraExpenses)}</td>
          <td className="px-3 py-2 text-right font-semibold">{formatCOP(record.netDelivered)}</td>
          <td className="px-3 py-2">
            <StatusSelect
              value={record.status}
              disabled={changeStatus.isPending}
              onChange={(status) => changeStatus.mutate({ id: record.id, version: record.version, status })}
            />
          </td>
          <td className="space-x-1 px-3 py-2 text-right whitespace-nowrap">
            <Button variant="ghost" onClick={() => setEditing(true)}>
              Editar
            </Button>
            <Button variant="ghost" disabled={remove.isPending} onClick={confirmDelete}>
              Eliminar
            </Button>
          </td>
        </tr>
        {errorRow}
      </>
    )
  }

  return (
    <>
      <tr className="bg-amber-50">
        <td className="px-3 py-2 whitespace-nowrap">{formatDate(record.date)}</td>
        <td className="px-3 py-2">{dayName(record.dayOfWeek)}</td>
        <td className="px-3 py-2">
          <input
            type="checkbox"
            checked={draft.picoPlaca}
            onChange={(e) => setDraft({ ...draft, picoPlaca: e.target.checked })}
            aria-label="Pico y placa"
          />
        </td>
        <td className="px-3 py-2 text-right">{numberCell('expected')}</td>
        <td className="px-3 py-2 text-right">{numberCell('received')}</td>
        <td className="px-3 py-2 text-right">{numberCell('savings')}</td>
        <td className="px-3 py-2 text-right">{numberCell('expenses')}</td>
        <td className={`px-3 py-2 text-right font-semibold ${previewNet < 0 ? 'text-red-600' : ''}`}>
          {formatCOP(previewNet)}
        </td>
        <td className="px-3 py-2">
          <select
            value={draft.status ?? ''}
            onChange={(e) => {
              const status = (e.target.value || null) as ComplianceStatus | null
              // Entregado: lo recibido pasa a ser lo esperado (si aún no lo alcanzaba)
              setDraft({
                ...draft,
                status,
                received: status === 'CUMPLIDO' && draft.received < draft.expected ? draft.expected : draft.received,
              })
            }}
            className="rounded border border-slate-300 bg-white px-2 py-1 text-xs"
            aria-label="Estado"
          >
            <option value="">Automático</option>
            {STATUS_OPTIONS.map((status) => (
              <option key={status} value={status}>
                {STATUS_LABELS[status]}
              </option>
            ))}
          </select>
        </td>
        <td className="space-x-1 px-3 py-2 text-right whitespace-nowrap">
          <Button disabled={invalid || update.isPending} onClick={save}>
            {update.isPending ? '…' : 'Guardar'}
          </Button>
          <Button variant="secondary" onClick={() => setEditing(false)}>
            Cancelar
          </Button>
        </td>
      </tr>
      {errorRow}
    </>
  )
}

function Pagination({
  page,
  totalPages,
  totalElements,
  onChange,
}: {
  page: number
  totalPages: number
  totalElements: number
  onChange: (page: number) => void
}) {
  if (totalPages <= 1) {
    return <p className="border-t border-slate-100 px-4 py-3 text-xs text-slate-500">{totalElements} registros</p>
  }
  return (
    <div className="flex items-center justify-between border-t border-slate-100 px-4 py-3 text-sm">
      <span className="text-xs text-slate-500">
        {totalElements} registros · página {page + 1} de {totalPages}
      </span>
      <div className="flex gap-2">
        <Button variant="secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>
          Anterior
        </Button>
        <Button variant="secondary" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>
          Siguiente
        </Button>
      </div>
    </div>
  )
}
