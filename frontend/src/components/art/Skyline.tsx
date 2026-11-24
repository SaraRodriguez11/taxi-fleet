/** Atardecer sobre Bogotá: cerros orientales (con la silueta de Monserrate) y edificios. */
const BUILDINGS: [number, number, number][] = [
  // x, ancho, alto
  [250, 46, 70], [300, 34, 105], [338, 52, 80], [394, 30, 130], [428, 44, 92], [476, 38, 150],
  [518, 56, 78], [578, 34, 112], [616, 48, 96], [668, 30, 168], [702, 52, 88], [758, 40, 124],
  [802, 56, 74], [862, 34, 140], [900, 48, 100], [952, 38, 84], [994, 52, 118], [1050, 34, 92],
  [1088, 60, 70], [1152, 48, 104],
]

export default function Skyline({ className = '' }: { className?: string }) {
  return (
    <svg viewBox="0 0 1200 260" preserveAspectRatio="xMidYMax slice" className={className} aria-hidden="true">
      <defs>
        <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#141b2d" />
          <stop offset="0.55" stopColor="#3b3f6e" />
          <stop offset="1" stopColor="#f2994a" />
        </linearGradient>
      </defs>
      <rect width="1200" height="260" fill="url(#sky)" />
      <circle cx="930" cy="170" r="46" fill="#ffd75e" opacity="0.9" />
      {/* cerros */}
      <path d="M0 260 V120 Q60 70 130 96 L170 70 L186 52 L196 70 Q240 90 270 130 V260 Z" fill="#232a4d" />
      <path d="M186 52 V40 M182 46 H190" stroke="#232a4d" strokeWidth="3" />
      {/* edificios */}
      {BUILDINGS.map(([x, w, h]) => (
        <g key={x}>
          <rect x={x} y={260 - h} width={w} height={h} fill="#1a2140" />
          {Array.from({ length: Math.floor(h / 22) }, (_, row) =>
            (x * 7 + row * 13) % 3 === 0 ? (
              <rect key={row} x={x + 8} y={260 - h + 10 + row * 22} width={w - 16} height="5" fill="#ffd75e" opacity="0.75" />
            ) : null,
          )}
        </g>
      ))}
    </svg>
  )
}
