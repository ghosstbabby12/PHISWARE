import { useRef } from 'react'
import { Link } from 'react-router-dom'
import { LucideIcon, ArrowUpRight } from 'lucide-react'

interface Props {
  to: string
  icon: LucideIcon
  title: string
  description: string
}

/** Módulo de acción con inclinación 3D sutil que sigue al cursor. */
export default function QuickActionCard({ to, icon: Icon, title, description }: Props) {
  const ref = useRef<HTMLAnchorElement>(null)

  function onMove(e: React.MouseEvent) {
    const el = ref.current
    if (!el || window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) return
    const r = el.getBoundingClientRect()
    const x = (e.clientX - r.left) / r.width - 0.5
    const y = (e.clientY - r.top) / r.height - 0.5
    el.style.transform = `perspective(700px) rotateX(${(-y * 6).toFixed(2)}deg) rotateY(${(x * 8).toFixed(2)}deg) translateY(-4px)`
    el.style.setProperty('--mx', `${(x + 0.5) * 100}%`)
    el.style.setProperty('--my', `${(y + 0.5) * 100}%`)
  }

  function onLeave() {
    if (ref.current) ref.current.style.transform = ''
  }

  return (
    <Link
      ref={ref}
      to={to}
      onMouseMove={onMove}
      onMouseLeave={onLeave}
      className="module group !p-5 flex flex-col gap-4 will-change-transform"
      style={{ transition: 'transform 200ms ease-out, box-shadow 300ms, border-color 300ms' }}
    >
      {/* Brillo que sigue al cursor */}
      <span
        className="pointer-events-none absolute inset-0 opacity-0 transition-opacity duration-300 group-hover:opacity-100"
        style={{ background: 'radial-gradient(240px circle at var(--mx, 50%) var(--my, 50%), rgba(180,35,69,0.12), transparent 70%)' }}
        aria-hidden
      />
      <span className="relative flex items-center justify-between">
        <span className="grid h-11 w-11 place-items-center rounded-2xl bg-gradient-to-br from-primary-500 to-primary-700 text-white shadow-[0_8px_18px_-6px_rgba(121,13,43,0.6)] transition-transform duration-300 group-hover:scale-110 group-hover:-rotate-6">
          <Icon className="h-5 w-5" />
        </span>
        <span className="grid h-8 w-8 place-items-center rounded-full bg-white/70 text-ink-500 ring-1 ring-ink-200/70 transition-all duration-300 group-hover:bg-primary-600 group-hover:text-white group-hover:ring-transparent group-hover:rotate-45 dark:bg-white/5 dark:ring-white/10">
          <ArrowUpRight className="h-4 w-4" />
        </span>
      </span>
      <span className="relative">
        <span className="block text-[15px] font-semibold text-ink-900 dark:text-ink-50">{title}</span>
        <span className="mt-0.5 block text-sm text-ink-500 dark:text-ink-300">{description}</span>
      </span>
    </Link>
  )
}
