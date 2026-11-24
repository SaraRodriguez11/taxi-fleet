import type { ReactNode } from 'react'
import RoadScene from './art/RoadScene'

/** Marco común del inicio de sesión y el registro: escena de la ciudad a un lado, formulario al otro. */
export default function AuthShell({ title, subtitle, children, footer }: {
  title: string
  subtitle: string
  children: ReactNode
  footer: ReactNode
}) {
  return (
    <div className="grid min-h-screen lg:grid-cols-[1.1fr_1fr]">
      <section className="relative flex min-h-64 flex-col justify-between overflow-hidden bg-slate-900 p-6 text-white sm:p-10 lg:min-h-screen">
        <RoadScene className="absolute inset-0" taxiClass="w-52 sm:w-72 lg:w-96" />
        <div aria-hidden="true" className="absolute inset-0 bg-gradient-to-r from-slate-950/80 via-slate-950/45 to-transparent" />
        <div className="relative flex items-center gap-3">
          <span className="grid h-10 w-10 place-items-center rounded-xl bg-amber-400 font-display text-lg font-extrabold text-slate-900">
            T
          </span>
          <span className="font-display text-lg font-semibold">Flota de Taxis</span>
        </div>
        <div className="relative max-w-md pb-24 sm:pb-32 lg:pb-48">
          <h2 className="font-display text-3xl font-extrabold leading-tight sm:text-5xl">
            Cada taxi, cada día, cada peso en su sitio.
          </h2>
          <p className="mt-3 hidden text-base text-white/80 sm:block">
            Controla la producción, el ahorro de tus conductores y el pico y placa de toda tu flota desde un solo lugar.
          </p>
        </div>
      </section>

      <main className="grid place-items-center px-5 py-10 sm:px-10">
        <div className="w-full max-w-sm">
          <h1 className="font-display text-3xl font-extrabold text-slate-900">{title}</h1>
          <p className="mb-7 mt-1.5 text-sm text-slate-500">{subtitle}</p>
          {children}
          <p className="mt-6 text-center text-sm text-slate-600">{footer}</p>
        </div>
      </main>
    </div>
  )
}
