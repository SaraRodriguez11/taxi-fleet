import Skyline from './Skyline'
import Taxi from './Taxi'

/** Escena completa: cielo, ciudad, vía y un taxi que llega. Se usa en el inicio de sesión y en Inicio. */
export default function RoadScene({ className = '', taxiClass = 'w-56 sm:w-72' }: { className?: string; taxiClass?: string }) {
  return (
    <div className={`overflow-hidden ${className}`} aria-hidden="true">
      <Skyline className="absolute inset-0 h-full w-full" />
      <div className="absolute inset-x-0 bottom-0 h-[22%] bg-slate-950" />
      <div className="absolute inset-x-0 bottom-[9%] h-[3px] overflow-hidden">
        <div className="road-dash h-full w-[calc(100%+60px)] bg-[repeating-linear-gradient(90deg,#ffc31f_0_30px,transparent_30px_60px)]" />
      </div>
      <div className="taxi-arrive absolute bottom-[10%] right-[8%]">
        <Taxi animated className={taxiClass} />
      </div>
    </div>
  )
}
