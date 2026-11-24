import { useState } from 'react'
import { Link } from 'react-router-dom'
import { usePicoPlaca, useReport, useSummary, useTaxis } from '../api/hooks'
import { useAuth } from '../auth/AuthContext'
import { errorMessage } from '../api/client'
import RoadScene from '../components/art/RoadScene'
import Taxi from '../components/art/Taxi'
import Plate from '../components/Plate'
import { Card, EmptyState, ErrorBanner, Loading, StatCard } from '../components/ui'
import { formatCOP, formatPercent, TODAY } from '../lib/format'

const currentYear = new Date().getFullYear()

export default function DashboardPage() {
  const [year, setYear] = useState(currentYear)
  const summary = useSummary(year)
  const monthly = useReport('monthly', year)
  const { owner } = useAuth()
  const taxis = useTaxis()
  const pico = usePicoPlaca(TODAY)

  return (
    <>
      <section className="relative mb-6 overflow-hidden rounded-3xl bg-slate-900 text-white shadow-card">
        <RoadScene className="absolute inset-0" taxiClass="w-40 sm:w-60" />
        <div className="relative flex flex-wrap items-start justify-between gap-4 p-6 pb-28 sm:p-8 sm:pb-36">
          <div>
            <h1 className="text-3xl font-extrabold sm:text-4xl">Hola, {owner?.fullName.split(' ')[0]}</h1>
            <p className="mt-1 text-white/80">
              {taxis.data
                ? `${taxis.data.filter((t) => t.active).length} taxis activos en tu flota`
                : 'Tu flota de un vistazo'}
            </p>
          </div>
          <div className="rounded-2xl bg-slate-950/55 px-4 py-3 backdrop-blur">
            <p className="text-sm text-white/80">Pico y placa hoy</p>
            <div className="mt-1.5 flex items-center gap-2">
              {pico.data && pico.data.restrictedDigits.length > 0 ? (
                pico.data.restrictedDigits.map((digit) => (
                  <span
                    key={digit}
                    className="grid h-10 w-10 place-items-center rounded-lg border-2 border-slate-900 bg-amber-400 font-display text-2xl font-extrabold text-slate-900"
                  >
                    {digit}
                  </span>
                ))
              ) : (
                <span className="text-sm font-semibold">{pico.data ? 'Sin restricción' : 'Consultando…'}</span>
              )}
            </div>
          </div>
        </div>
      </section>

      <div className="mb-4 flex justify-end">
        <YearInput value={year} onChange={setYear} />
      </div>

      {summary.isPending && <Loading />}
      {summary.isError && <ErrorBanner message={errorMessage(summary.error)} />}

      {summary.data && (
        <section className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <StatCard label="Producción esperada" value={formatCOP(summary.data.totals.expected)} />
          <StatCard
            label="Producción recibida"
            value={formatCOP(summary.data.totals.received)}
            hint={`Cumplimiento ${formatPercent(summary.data.complianceRate)}`}
          />
          <StatCard label="Ahorro de conductores" value={formatCOP(summary.data.totals.savings)} />
          <StatCard label="Gastos extra" value={formatCOP(summary.data.totals.expenses)} />
          <StatCard label="Neto entregado" value={formatCOP(summary.data.totals.net)} hint="Recibida − ahorro − gastos" />
          <StatCard
            label="Flota activa"
            value={`${summary.data.activeTaxis} taxis`}
            hint={`${summary.data.activeDrivers} conductores activos`}
          />
        </section>
      )}

      <Card className="mt-6 p-5">
        <div className="mb-4 flex flex-wrap items-baseline justify-between gap-2">
          <h2 className="font-semibold text-slate-900">Mis vehículos</h2>
        </div>
        {taxis.isPending && <Loading />}
        {taxis.isError && <ErrorBanner message={errorMessage(taxis.error)} />}
        {taxis.data && taxis.data.length === 0 && (
          <EmptyState>
            Aún no tienes vehículos.{' '}
            <Link to="/taxis" className="font-semibold text-amber-600 hover:underline">
              Registra el primero
            </Link>
          </EmptyState>
        )}
        {taxis.data && taxis.data.length > 0 && (
          <ul className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            {taxis.data.map((taxi) => {
              const restricted = pico.data?.restrictedDigits.includes(Number(taxi.plate.slice(-1))) ?? false
              return (
                <li
                  key={taxi.id}
                  className={`group rounded-2xl border p-4 text-sm transition-all hover:-translate-y-0.5 hover:shadow-card ${
                    restricted ? 'border-sky-200 bg-sky-50' : 'border-slate-200 bg-white'
                  } ${taxi.active ? '' : 'opacity-60'}`}
                >
                  <Taxi className={`mx-auto h-16 w-auto transition-transform group-hover:translate-x-1 ${restricted ? 'grayscale' : ''}`} />
                  <div className="mt-3 flex items-center justify-between gap-2">
                    <Plate value={taxi.plate} />
                    {restricted && (
                      <span className="rounded-full bg-sky-100 px-2.5 py-1 text-xs font-semibold text-sky-800">
                        No circula hoy
                      </span>
                    )}
                    {!taxi.active && <span className="text-xs text-slate-500">Inactivo</span>}
                  </div>
                  <p className="mt-2 font-medium text-slate-800">{taxi.model}</p>
                  <p className="text-slate-500">{taxi.driverName ?? 'Sin conductor asignado'}</p>
                </li>
              )
            })}
          </ul>
        )}
      </Card>

      <Card className="mt-6 p-5">
        <h2 className="mb-4 font-semibold text-slate-900">Neto entregado por mes</h2>
        {monthly.isPending && <Loading />}
        {monthly.isError && <ErrorBanner message={errorMessage(monthly.error)} />}
        {monthly.data && monthly.data.length === 0 && (
          <EmptyState>Aún no hay registros en {year}. Genera el control diario desde “Control diario”.</EmptyState>
        )}
        {monthly.data && monthly.data.length > 0 && <MonthlyBars periods={monthly.data} />}
      </Card>
    </>
  )
}

function MonthlyBars({ periods }: { periods: { label: string; totals: { net: number } }[] }) {
  const max = Math.max(...periods.map((p) => p.totals.net), 1)
  return (
    <ul className="space-y-2">
      {periods.map((period) => (
        <li key={period.label} className="flex items-center gap-3 text-sm">
          <span className="w-24 shrink-0 text-slate-600">{period.label}</span>
          <div className="h-5 flex-1 overflow-hidden rounded bg-slate-100">
            <div
              className="h-full rounded bg-amber-400"
              style={{ width: `${(period.totals.net / max) * 100}%` }}
              role="presentation"
            />
          </div>
          <span className="tabular w-32 shrink-0 text-right font-medium">{formatCOP(period.totals.net)}</span>
        </li>
      ))}
    </ul>
  )
}

export function YearInput({ value, onChange }: { value: number; onChange: (year: number) => void }) {
  return (
    <label className="flex items-center gap-2 text-sm text-slate-600">
      Año
      <input
        type="number"
        min={2000}
        max={2100}
        value={value}
        onChange={(event) => {
          const next = Number(event.target.value)
          if (next >= 2000 && next <= 2100) onChange(next)
        }}
        className="w-24 rounded-lg border border-slate-300 bg-white px-3 py-2"
      />
    </label>
  )
}
