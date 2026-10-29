import { Link } from 'react-router-dom'
import { LucideIcon, ArrowUpRight } from 'lucide-react'

interface Props {
  to: string
  icon: LucideIcon
  title: string
  description: string
}

export default function QuickActionCard({ to, icon: Icon, title, description }: Props) {
  return (
    <Link to={to} className="card-hover group !p-5 flex items-start gap-4">
      <span className="p-2.5 bg-primary-100 rounded-xl border border-primary-600/10 shrink-0 group-hover:bg-primary-600 group-hover:text-white transition-colors">
        <Icon className="w-5 h-5 text-primary-700 group-hover:text-white" />
      </span>
      <span className="flex-1 min-w-0">
        <span className="flex items-center gap-1.5 font-semibold text-ink-900 text-[15px]">
          {title}
          <ArrowUpRight className="w-4 h-4 text-ink-300 group-hover:text-primary-600 transition-colors" />
        </span>
        <span className="block text-sm text-ink-500 mt-0.5">{description}</span>
      </span>
    </Link>
  )
}
