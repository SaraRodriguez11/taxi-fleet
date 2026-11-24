/** Placa colombiana de servicio público: fondo amarillo y letras negras. */
export default function Plate({ value, className = '' }: { value: string; className?: string }) {
  const clean = value.replace(/[^A-Za-z0-9]/g, '').toUpperCase()
  const text = clean.length === 6 ? `${clean.slice(0, 3)} ${clean.slice(3)}` : clean
  return (
    <span
      className={`inline-block rounded-md border-2 border-slate-900 bg-amber-400 px-2.5 py-0.5 font-display text-lg font-extrabold tracking-wider text-slate-900 shadow-[inset_0_-2px_0_rgb(0_0_0/0.12)] ${className}`}
    >
      {text}
    </span>
  )
}
