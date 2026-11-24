import { useState, type FormEvent } from 'react'
import { ApiError, errorMessage } from '../api/client'
import { useDeactivateDriver, useDrivers, useSaveDriver } from '../api/hooks'
import type { Driver, DriverInput } from '../api/types'
import { Button, Card, EmptyState, ErrorBanner, Field, Loading, PageHeader } from '../components/ui'

const EMPTY: DriverInput = { fullName: '', documentNumber: '', phone: '', active: true }

export default function DriversPage() {
  const drivers = useDrivers()
  const [editing, setEditing] = useState<{ id: number | null; input: DriverInput } | null>(null)
  const deactivate = useDeactivateDriver()

  const startEdit = (driver: Driver) =>
    setEditing({
      id: driver.id,
      input: {
        fullName: driver.fullName,
        documentNumber: driver.documentNumber,
        phone: driver.phone ?? '',
        active: driver.active,
      },
    })

  return (
    <>
      <PageHeader
        title="Conductores"
        subtitle="Datos personales y estado de actividad"
        actions={<Button onClick={() => setEditing({ id: null, input: EMPTY })}>Nuevo conductor</Button>}
      />

      {editing && (
        <DriverForm key={editing.id ?? 'new'} id={editing.id} initial={editing.input} onClose={() => setEditing(null)} />
      )}

      {deactivate.isError && <ErrorBanner message={errorMessage(deactivate.error)} />}
      {drivers.isPending && <Loading />}
      {drivers.isError && <ErrorBanner message={errorMessage(drivers.error)} />}

      {drivers.data && (
        <Card className="overflow-x-auto">
          {drivers.data.length === 0 ? (
            <EmptyState>Aún no hay conductores registrados.</EmptyState>
          ) : (
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Nombre</th>
                  <th className="px-4 py-3">Documento</th>
                  <th className="px-4 py-3">Teléfono</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {drivers.data.map((driver) => (
                  <tr key={driver.id} className={driver.active ? '' : 'text-slate-400'}>
                    <td className="px-4 py-3 font-semibold">{driver.fullName}</td>
                    <td className="px-4 py-3">{driver.documentNumber}</td>
                    <td className="px-4 py-3">{driver.phone ?? '—'}</td>
                    <td className="px-4 py-3">{driver.active ? 'Activo' : 'Inactivo'}</td>
                    <td className="space-x-1 px-4 py-3 text-right">
                      <Button variant="ghost" onClick={() => startEdit(driver)}>
                        Editar
                      </Button>
                      {driver.active && (
                        <Button
                          variant="ghost"
                          onClick={() => {
                            if (confirm(`¿Dar de baja a ${driver.fullName}?`)) deactivate.mutate(driver.id)
                          }}
                        >
                          Dar de baja
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      )}
    </>
  )
}

function DriverForm({ id, initial, onClose }: { id: number | null; initial: DriverInput; onClose: () => void }) {
  const [input, setInput] = useState(initial)
  const save = useSaveDriver()
  const fieldErrors = save.error instanceof ApiError ? save.error.fieldErrors : {}

  const submit = (event: FormEvent) => {
    event.preventDefault()
    save.mutate({ id, input }, { onSuccess: onClose })
  }

  return (
    <Card className="mb-5 p-5">
      <h2 className="mb-4 font-semibold">{id === null ? 'Nuevo conductor' : 'Editar conductor'}</h2>
      <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
        <Field
          label="Nombre completo"
          value={input.fullName}
          error={fieldErrors.fullName}
          onChange={(e) => setInput({ ...input, fullName: e.target.value })}
          required
        />
        <Field
          label="Documento"
          value={input.documentNumber}
          error={fieldErrors.documentNumber}
          onChange={(e) => setInput({ ...input, documentNumber: e.target.value })}
          required
        />
        <Field
          label="Teléfono"
          value={input.phone}
          error={fieldErrors.phone}
          onChange={(e) => setInput({ ...input, phone: e.target.value })}
        />
        {id !== null && (
          <label className="flex items-center gap-2 self-end pb-2 text-sm">
            <input
              type="checkbox"
              checked={input.active}
              onChange={(e) => setInput({ ...input, active: e.target.checked })}
            />
            Conductor activo
          </label>
        )}
        <div className="flex items-center gap-2 sm:col-span-2">
          <Button type="submit" disabled={save.isPending}>
            {save.isPending ? 'Guardando…' : 'Guardar'}
          </Button>
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancelar
          </Button>
        </div>
        {save.isError && Object.keys(fieldErrors).length === 0 && (
          <div className="sm:col-span-2">
            <ErrorBanner message={errorMessage(save.error)} />
          </div>
        )}
      </form>
    </Card>
  )
}
