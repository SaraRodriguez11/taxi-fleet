import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const links = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/control-diario', label: 'Control diario', end: false },
  { to: '/taxis', label: 'Taxis', end: false },
  { to: '/conductores', label: 'Conductores', end: false },
  { to: '/reportes', label: 'Reportes', end: false },
]

export default function Layout() {
  const { owner, logout } = useAuth()
  return (
    <div className="flex min-h-screen flex-col md:flex-row">
      <aside className="relative flex flex-col bg-slate-900 text-slate-200 md:w-64 md:shrink-0">
        <div className="flex items-center gap-2 px-5 py-5">
          <span className="grid h-10 w-10 place-items-center rounded-xl bg-amber-400 font-display text-lg font-extrabold text-slate-900">
            T
          </span>
          <div className="leading-tight">
            <p className="font-display text-lg font-bold text-white">Flota de Taxis</p>
            <p className="text-xs text-slate-400">Control contable</p>
          </div>
        </div>
        <nav className="flex gap-1 overflow-x-auto px-3 pb-3 md:flex-col md:pb-0">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.end}
              className={({ isActive }) =>
                `whitespace-nowrap rounded-xl px-3.5 py-2.5 text-sm font-semibold transition-colors ${
                  isActive ? 'bg-amber-400 text-slate-900' : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                }`
              }
            >
              {link.label}
            </NavLink>
          ))}
          <button
            onClick={logout}
            className="whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 md:hidden"
          >
            Cerrar sesión
          </button>
        </nav>
        <div className="mt-auto hidden border-t border-slate-800 p-4 md:block">
          <p className="truncate text-sm font-medium text-white">{owner?.fullName}</p>
          <p className="truncate text-xs text-slate-400">{owner?.email}</p>
          <button
            onClick={logout}
            className="mt-3 w-full rounded-lg border border-slate-700 px-3 py-2 text-sm font-medium text-slate-200 hover:bg-slate-800"
          >
            Cerrar sesión
          </button>
        </div>
        <div
          aria-hidden="true"
          className="absolute inset-y-0 right-0 hidden w-[3px] bg-[repeating-linear-gradient(180deg,#ffc31f_0_18px,transparent_18px_36px)] opacity-60 md:block"
        />
      </aside>

      <main className="min-w-0 flex-1 p-4 md:p-8">
        <Outlet />
      </main>
    </div>
  )
}
