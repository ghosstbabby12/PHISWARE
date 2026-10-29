import { LucideIcon } from 'lucide-react'
import clsx from 'clsx'

interface Props {
  title: string
  value: string | number
  icon: LucideIcon
  iconClass?: string
  subtitle?: string
}

export default function StatCard({ title, value, icon: Icon, iconClass = 'text-primary-700', subtitle }: Props) {
  return (
    <div className="stat-card">
      <div className="flex items-center justify-between">
        <p className="text-sm text-ink-500 font-medium">{title}</p>
        <span className="p-2 bg-primary-100 rounded-xl border border-primary-600/10">
          <Icon className={clsx('w-4.5 h-4.5 w-5 h-5', iconClass)} />
        </span>
      </div>
      <p className="text-3xl font-bold text-ink-900 mt-1 tabular-nums">{value}</p>
      {subtitle && <p className="text-xs text-ink-500">{subtitle}</p>}
    </div>
  )
}
