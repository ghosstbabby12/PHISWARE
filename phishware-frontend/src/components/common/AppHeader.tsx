import { ReactNode } from 'react'
import Logo from './Logo'

interface Props {
  title: string
  subtitle?: string
  actions?: ReactNode
  compact?: boolean
}

/** Encabezado consistente: identidad + titulo + acciones. */
export default function AppHeader({ title, subtitle, actions, compact = false }: Props) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-4">
      <div className="flex items-start gap-4">
        <div className="hidden sm:block mt-0.5">
          <Logo size={compact ? 32 : 40} showWordmark={false} />
        </div>
        <div>
          <h1 className="section-title">{title}</h1>
          {subtitle && <p className="section-subtitle">{subtitle}</p>}
        </div>
      </div>
      {actions && <div className="flex items-center gap-2 flex-wrap">{actions}</div>}
    </div>
  )
}
