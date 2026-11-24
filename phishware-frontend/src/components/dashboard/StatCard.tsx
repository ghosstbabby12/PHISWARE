import { useEffect, useState } from 'react'
import { LucideIcon } from 'lucide-react'
import clsx from 'clsx'

interface Props {
  title: string
  value: number
  icon: LucideIcon
  iconClass?: string
  subtitle?: string
  /** 0-100: barra de proporción opcional bajo el número */
  ratio?: number
  barClass?: string
}

/** Cuenta de 0 al valor final con easing; respeta prefers-reduced-motion. */
export function useCountUp(target: number, duration = 900) {
  const [value, setValue] = useState(0)
  useEffect(() => {
    if (window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) {
      setValue(target)
      return
    }
    let frame = 0
    const start = performance.now()
    const tick = (now: number) => {
      const p = Math.min((now - start) / duration, 1)
      setValue(Math.round(target * (1 - Math.pow(1 - p, 3))))
      if (p < 1) frame = requestAnimationFrame(tick)
    }
    frame = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(frame)
  }, [target, duration])
  return value
}

export default function StatCard({ title, value, icon: Icon, iconClass = 'text-primary-700', subtitle, ratio, barClass = 'bg-primary-600' }: Props) {
  const shown = useCountUp(value)
  return (
    <div className="stat-card group">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-ink-500 dark:text-ink-300">{title}</p>
        <span className="grid h-10 w-10 place-items-center rounded-2xl bg-white/80 shadow-sm ring-1 ring-ink-200/60 transition-transform duration-300 group-hover:-rotate-6 group-hover:scale-110 dark:bg-white/5 dark:ring-white/10">
          <Icon className={clsx('h-5 w-5', iconClass)} />
        </span>
      </div>
      <p className="mt-1 text-3xl font-bold tabular-nums text-ink-900 dark:text-ink-50">{shown.toLocaleString()}</p>
      {ratio !== undefined && (
        <div className="h-1.5 overflow-hidden rounded-full bg-ink-100 dark:bg-white/10">
          <div className={clsx('h-full rounded-full transition-all duration-1000 ease-out', barClass)} style={{ width: `${Math.min(ratio, 100)}%` }} />
        </div>
      )}
      {subtitle && <p className="text-xs text-ink-500 dark:text-ink-300">{subtitle}</p>}
    </div>
  )
}
