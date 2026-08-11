import { LucideIcon } from 'lucide-react'
import clsx from 'clsx'

interface Props {
  title: string
  value: string | number
  icon: LucideIcon
  colorClass?: string
  subtitle?: string
}

export default function StatCard({ title, value, icon: Icon, colorClass = 'text-primary-400', subtitle }: Props) {
  return (
    <div className="stat-card">
      <div className="flex items-center justify-between">
        <p className="text-sm text-slate-400 font-medium">{title}</p>
        <Icon className={clsx('w-5 h-5', colorClass)} />
      </div>
      <p className="text-3xl font-bold text-white mt-1">{value}</p>
      {subtitle && <p className="text-xs text-slate-500">{subtitle}</p>}
    </div>
  )
}
