import { jsPDF } from 'jspdf'
import autoTable from 'jspdf-autotable'
import type { PeriodReport } from '../api/types'
import { formatCOP, formatDate } from './format'

const COLUMNS = ['Taxi', 'Esperada', 'Recibida', 'Ahorro', 'Gastos extra', 'Neto entregado']

const money = (value: number) => formatCOP(value)

const kindLabelOf = (kind: 'weekly' | 'monthly') => (kind === 'weekly' ? 'semanal' : 'mensual')

export const reportFileName = (kind: 'weekly' | 'monthly', year: number) =>
  `reporte-${kindLabelOf(kind)}-${year}.pdf`

/** Descarga el PDF en el navegador. */
export function downloadReportPdf(kind: 'weekly' | 'monthly', year: number, periods: PeriodReport[]) {
  buildReportPdf(kind, year, periods).save(reportFileName(kind, year))
}

/**
 * Construye el reporte consolidado (semanal o mensual): una tabla por periodo del año
 * con el desglose por taxi y una fila de totales; los periodos sin registros se indican.
 */
function buildReportPdf(kind: 'weekly' | 'monthly', year: number, periods: PeriodReport[]): jsPDF {
  const kindLabel = kindLabelOf(kind)
  const doc = new jsPDF({ orientation: 'landscape', unit: 'mm', format: 'a4' })

  doc.setFontSize(16)
  doc.text(`Reporte ${kindLabel} consolidado ${year}`, 14, 16)
  doc.setFontSize(9)
  doc.setTextColor(100)
  doc.text(`Generado el ${new Date().toLocaleDateString('es-CO')}`, 14, 22)
  doc.setTextColor(0)

  let startY = 28
  for (const period of periods) {
    autoTable(doc, {
      startY,
      theme: 'grid',
      head: [
        [
          {
            content: `${period.label}  (${formatDate(period.start)} - ${formatDate(period.end)})`,
            colSpan: COLUMNS.length,
            styles: { halign: 'left', fillColor: [30, 41, 59], textColor: 255 },
          },
        ],
        COLUMNS,
      ],
      body: (period.taxis.length === 0
        ? [[{ content: 'Sin registros en este periodo', colSpan: COLUMNS.length, styles: { textColor: 130, fontStyle: 'italic' as const } }]]
        : period.taxis.map((taxi) => [
        taxi.plate,
        money(taxi.totals.expected),
        money(taxi.totals.received),
        money(taxi.totals.savings),
        money(taxi.totals.expenses),
        money(taxi.totals.net),
      ])) as never,
      foot: [
        [
          'TOTAL',
          money(period.totals.expected),
          money(period.totals.received),
          money(period.totals.savings),
          money(period.totals.expenses),
          money(period.totals.net),
        ],
      ],
      styles: { fontSize: 8, cellPadding: 1.8 },
      headStyles: { fillColor: [71, 85, 105] },
      footStyles: { fillColor: [251, 191, 36], textColor: 20, fontStyle: 'bold' },
      columnStyles: {
        1: { halign: 'right' },
        2: { halign: 'right' },
        3: { halign: 'right' },
        4: { halign: 'right' },
        5: { halign: 'right' },
      },
      margin: { left: 14, right: 14 },
    })
    startY = (doc as unknown as { lastAutoTable: { finalY: number } }).lastAutoTable.finalY + 8
  }

  if (periods.length === 0) {
    doc.setFontSize(11)
    doc.text('No hay registros para el periodo seleccionado.', 14, startY)
  }

  return doc
}
