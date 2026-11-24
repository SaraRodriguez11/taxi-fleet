import { useState } from 'react'
import { errorMessage } from '../api/client'
import { useReport } from '../api/hooks'
import type { PeriodReport } from '../api/types'
import { Button, Card, EmptyState, ErrorBanner, Loading, PageHeader } from '../components/ui'
import { formatCOP, formatDate } from '../lib/format'
import { YearInput } from './DashboardPage'

type Kind = 'weekly' | 'monthly'

const CURRENT_YEAR = new Date().getFullYear()

export default function ReportsPage() {
  const [kind, setKind] = useState<Kind>('monthly')
  const [year, setYear] = useState(CURRENT_YEAR)
  const report = useReport(kind, year)
  const hasData = !!report.data && report.data.some((p) => p.taxis.length > 0)

  return (
    <>
      <PageHeader
        title="Reportes consolidados"
        subtitle="Todos los meses o semanas del año, con el desglose por taxi"
        actions={
          <>
            <YearInput value={year} onChange={setYear} />
            <Button
              variant="secondary"
              disabled={!hasData}
              onClick={async () => {
                if (!report.data) return
                // jsPDF pesa bastante: se carga solo cuando se pide el PDF
                const { downloadReportPdf } = await import('../lib/pdf')
                downloadReportPdf(kind, year, report.data)
              }}
            >
              Descargar PDF
            </Button>
          </>
        }
      />

      <div className="mb-4 inline-flex rounded-lg border border-slate-300 bg-white p-1 text-sm font-medium">
        {(['monthly', 'weekly'] as const).map((option) => (
          <button
            key={option}
            onClick={() => setKind(option)}
            className={`rounded-md px-4 py-1.5 ${kind === option ? 'bg-slate-900 text-white' : 'text-slate-600'}`}
          >
            {option === 'monthly' ? 'Mensual' : 'Semanal'}
          </button>
        ))}
      </div>

      {report.isPending && <Loading />}
      {report.isError && <ErrorBanner message={errorMessage(report.error)} />}
      {report.data && !hasData && (
        <Card className="mb-5">
          <EmptyState>No hay registros contables en {year}. Genera el control diario para ver los reportes.</EmptyState>
        </Card>
      )}

      <div className="space-y-5">
        {report.data?.map((period) => <PeriodTable key={period.start} period={period} />)}
      </div>
    </>
  )
}

function PeriodTable({ period }: { period: PeriodReport }) {
  const empty = period.taxis.length === 0
  return (
    <Card className={`overflow-hidden ${empty ? 'opacity-60' : ''}`}>
      <div className="flex flex-wrap items-baseline justify-between gap-2 border-b border-slate-200 bg-slate-50 px-4 py-3">
        <h2 className="font-semibold text-slate-900">{period.label}</h2>
        <span className="text-xs text-slate-500">
          {formatDate(period.start)} – {formatDate(period.end)}
        </span>
      </div>
      {empty ? (
        <p className="px-4 py-3 text-sm italic text-slate-500">Sin registros en este periodo</p>
      ) : (
      <div className="overflow-x-auto">
        <table className="tabular w-full text-sm">
          <thead className="text-left text-xs uppercase text-slate-500">
            <tr>
              <th className="px-4 py-2">Taxi</th>
              <th className="px-4 py-2 text-right">Esperada</th>
              <th className="px-4 py-2 text-right">Recibida</th>
              <th className="px-4 py-2 text-right">Ahorro</th>
              <th className="px-4 py-2 text-right">Gastos extra</th>
              <th className="px-4 py-2 text-right">Neto entregado</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {period.taxis.map((taxi) => (
              <tr key={taxi.taxiId}>
                <td className="px-4 py-2 font-medium">{taxi.plate}</td>
                <td className="px-4 py-2 text-right">{formatCOP(taxi.totals.expected)}</td>
                <td className="px-4 py-2 text-right">{formatCOP(taxi.totals.received)}</td>
                <td className="px-4 py-2 text-right">{formatCOP(taxi.totals.savings)}</td>
                <td className="px-4 py-2 text-right">{formatCOP(taxi.totals.expenses)}</td>
                <td className="px-4 py-2 text-right font-medium">{formatCOP(taxi.totals.net)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot className="bg-amber-50 font-semibold">
            <tr>
              <td className="px-4 py-2">Total</td>
              <td className="px-4 py-2 text-right">{formatCOP(period.totals.expected)}</td>
              <td className="px-4 py-2 text-right">{formatCOP(period.totals.received)}</td>
              <td className="px-4 py-2 text-right">{formatCOP(period.totals.savings)}</td>
              <td className="px-4 py-2 text-right">{formatCOP(period.totals.expenses)}</td>
              <td className="px-4 py-2 text-right">{formatCOP(period.totals.net)}</td>
            </tr>
          </tfoot>
        </table>
      </div>
      )}
    </Card>
  )
}
