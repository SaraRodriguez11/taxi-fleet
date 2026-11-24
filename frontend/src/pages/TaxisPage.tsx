import { useState, type FormEvent } from 'react'
import { ApiError, errorMessage } from '../api/client'
import { useDeactivateTaxi, useDrivers, useSaveTaxi, useTaxis } from '../api/hooks'
import type { Taxi, TaxiInput } from '../api/types'
import Plate from '../components/Plate'
import { Button, Card, EmptyState, ErrorBanner, Field, Loading, PageHeader } from '../components/ui'

const EMPTY: TaxiInput = { plate: '', model: '', driverId: null, active: true }

export default function TaxisPage() {
  const taxis = useTaxis()
  const [editing, setEditing] = useState<{ id: number | null; input: TaxiInput } | null>(null)
  const deactivate = useDeactivateTaxi()

  const startEdit = (taxi: Taxi) =>
    setEditing({
      id: taxi.id,
      input: {
        plate: taxi.plate,
        model: taxi.model,
        driverId: taxi.driverId,
        active: taxi.active,
      },
    })

  return (
    <>
      <PageHeader
        title="Taxis"
        subtitle="Vehículos de la flota"
        actions={<Button onClick={() => setEditing({ id: null, input: EMPTY })}>Nuevo taxi</Button>}
      />

      {editing && <TaxiForm key={editing.id ?? 'new'} id={editing.id} initial={editing.input} onClose={() => setEditing(null)} />}

      {deactivate.isError && <ErrorBanner message={errorMessage(deactivate.error)} />}
      {taxis.isPending && <Loading />}
      {taxis.isError && <ErrorBanner message={errorMessage(taxis.error)} />}

      {taxis.data && (
        <Card className="overflow-x-auto">
          {taxis.data.length === 0 ? (
            <EmptyState>Aún no hay taxis registrados.</EmptyState>
          ) : (
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr>
                  <th className="px-4 py-3">Placa</th>
                  <th className="px-4 py-3">Modelo</th>
                  <th className="px-4 py-3">Conductor</th>
                  <th className="px-4 py-3">Estado</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {taxis.data.map((taxi) => (
                  <tr key={taxi.id} className={taxi.active ? '' : 'text-slate-400'}>
                    <td className="px-4 py-3"><Plate value={taxi.plate} /></td>
                    <td className="px-4 py-3">{taxi.model}</td>
                    <td className="px-4 py-3">{taxi.driverName ?? '—'}</td>
                    <td className="px-4 py-3">{taxi.active ? 'Activo' : 'Inactivo'}</td>
                    <td className="space-x-1 px-4 py-3 text-right">
                      <Button variant="ghost" onClick={() => startEdit(taxi)}>
                        Editar
                      </Button>
                      {taxi.active && (
                        <Button
                          variant="ghost"
                          onClick={() => {
                            if (confirm(`¿Dar de baja el taxi ${taxi.plate}? Se conserva su historial.`)) {
                              deactivate.mutate(taxi.id)
                            }
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

function TaxiForm({ id, initial, onClose }: { id: number | null; initial: TaxiInput; onClose: () => void }) {
  const [input, setInput] = useState(initial)
  const drivers = useDrivers(true)
  const save = useSaveTaxi()
  const fieldErrors = save.error instanceof ApiError ? save.error.fieldErrors : {}

  const submit = (event: FormEvent) => {
    event.preventDefault()
    save.mutate({ id, input }, { onSuccess: onClose })
  }

  return (
    <Card className="mb-5 p-5">
      <h2 className="mb-4 font-semibold">{id === null ? 'Nuevo taxi' : 'Editar taxi'}</h2>
      <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
        <Field
          label="Placa"
          value={input.plate}
          maxLength={6}
          placeholder="ABC123"
          error={fieldErrors.plate}
          onChange={(e) => setInput({ ...input, plate: e.target.value.toUpperCase() })}
          required
        />
        <Field
          label="Modelo"
          value={input.model}
          error={fieldErrors.model}
          onChange={(e) => setInput({ ...input, model: e.target.value })}
          required
        />
        <label className="block text-sm">
          <span className="mb-1 block font-medium text-slate-700">Conductor asignado</span>
          <select
            value={input.driverId ?? ''}
            onChange={(e) => setInput({ ...input, driverId: e.target.value ? Number(e.target.value) : null })}
            className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
          >
            <option value="">Sin asignar</option>
            {drivers.data?.map((driver) => (
              <option key={driver.id} value={driver.id}>
                {driver.fullName}
              </option>
            ))}
          </select>
        </label>
        {id !== null && (
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={input.active}
              onChange={(e) => setInput({ ...input, active: e.target.checked })}
            />
            Taxi activo
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
