/** Taxi de perfil dibujado a mano en SVG (ilustración original, sin imágenes externas). */
export default function Taxi({ className = '', animated = false }: { className?: string; animated?: boolean }) {
  const wheel = animated ? 'taxi-wheel' : ''
  return (
    <svg viewBox="0 0 240 100" className={className} role="img" aria-label="Taxi amarillo">
      <ellipse cx="120" cy="92" rx="104" ry="5" fill="#0d1220" opacity="0.28" />
      {/* carrocería */}
      <path
        d="M10 72 V58 Q10 50 20 48 L52 44 L72 25 Q76 21 82 21 H150 Q158 21 162 27 L180 44 L214 48 Q228 50 228 60 V72 Z"
        fill="#ffc31f"
      />
      <path d="M10 62 H228" stroke="#141b2d" strokeWidth="2" opacity="0.18" />
      {/* vidrios */}
      <path d="M78 29 H107 V43 H62 Z" fill="#cfe3f5" />
      <path d="M113 29 H148 Q152 29 154 32 L168 43 H113 Z" fill="#cfe3f5" />
      <path d="M78 29 H107 V33 L70 43 H62 Z" fill="#ffffff" opacity="0.35" />
      {/* aviso del techo */}
      <rect x="100" y="12" width="28" height="9" rx="2.5" fill="#141b2d" />
      <text x="114" y="19" textAnchor="middle" fontSize="6.5" fontWeight="800" fill="#ffc31f" fontFamily="sans-serif">
        TAXI
      </text>
      {/* manijas y espejo */}
      <rect x="108" y="48" width="9" height="2.4" rx="1.2" fill="#141b2d" opacity="0.5" />
      <rect x="152" y="48" width="9" height="2.4" rx="1.2" fill="#141b2d" opacity="0.5" />
      {/* luces */}
      <rect x="10" y="52" width="6" height="8" rx="2" fill="#d64545" />
      <rect x="220" y="52" width="8" height="7" rx="2" fill="#fff6cf" />
      <g className={animated ? 'taxi-lights' : undefined}>
        <path d="M228 56 L240 50 V66 Z" fill="#fff6cf" opacity="0.55" />
      </g>
      {/* ruedas */}
      {[58, 184].map((cx) => (
        <g key={cx}>
          <circle cx={cx} cy="74" r="19" fill="#0d1220" />
          <g className={wheel}>
            <circle cx={cx} cy="74" r="14" fill="#1d2740" />
            <circle cx={cx} cy="74" r="7" fill="#c9d1e0" />
            <path d={`M${cx} 61 V87 M${cx - 13} 74 H${cx + 13}`} stroke="#8d98b0" strokeWidth="2" />
          </g>
        </g>
      ))}
    </svg>
  )
}
