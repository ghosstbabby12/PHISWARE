import { CheckCircle, AlertTriangle, XCircle } from 'lucide-react'
import { RiskLevel } from '@/types/analysis.types'
import clsx from 'clsx'

interface Props {
  level: RiskLevel
  className?: string
}

const config = {
  SAFE: {
    cls: 'badge-safe',
    icon: CheckCircle,
    label: 'Seguro',
  },
  SUSPICIOUS: {
    cls: 'badge-suspicious',
    icon: AlertTriangle,
    label: 'Sospechoso',
  },
  DANGEROUS: {
    cls: 'badge-dangerous',
    icon: XCircle,
    label: 'Peligroso',
  },
}

export default function RiskBadge({ level, className }: Props) {
  const { cls, icon: Icon, label } = config[level]
  return (
    <span className={clsx(cls, className)}>
      <Icon className="w-3 h-3" />
      {label}
    </span>
  )
}
