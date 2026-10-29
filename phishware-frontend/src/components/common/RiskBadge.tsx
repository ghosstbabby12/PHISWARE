import { CheckCircle, AlertTriangle, XCircle } from 'lucide-react'
import { RiskLevel } from '@/types/analysis.types'
import { useLang } from '@/i18n/LanguageContext'
import clsx from 'clsx'

interface Props {
  level: RiskLevel
  className?: string
}

const config = {
  SAFE: { cls: 'badge-safe', icon: CheckCircle },
  SUSPICIOUS: { cls: 'badge-suspicious', icon: AlertTriangle },
  DANGEROUS: { cls: 'badge-dangerous', icon: XCircle },
}

export default function RiskBadge({ level, className }: Props) {
  const { t } = useLang()
  const { cls, icon: Icon } = config[level]
  return (
    <span className={clsx(cls, className)}>
      <Icon className="w-3 h-3" aria-hidden />
      {t.analysis.verdict[level]}
    </span>
  )
}
