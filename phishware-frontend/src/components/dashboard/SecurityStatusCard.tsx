import { Link } from 'react-router-dom'
import { ArrowRight, Bell, ShieldCheck } from 'lucide-react'
import RiskBadge from '@/components/common/RiskBadge'
import { useLang } from '@/i18n/LanguageContext'

interface Props {
  threatsDetected: number
  unreadAlerts: number
  totalAnalyses: number
}

/** Estado de protección derivado SOLO de datos reales del backend. */
export default function SecurityStatusCard({ threatsDetected, unreadAlerts, totalAnalyses }: Props) {
  const { t } = useLang()
  const calm = threatsDetected === 0 && unreadAlerts === 0
  return (
    <div className="card !p-0 overflow-hidden">
      <div className="bg-primary-800 px-6 py-5 flex items-start justify-between gap-4">
        <div>
          <p className="text-white/70 text-xs font-semibold uppercase tracking-wider">{t.dashboard.secured}</p>
          <p className="text-white font-bold text-xl mt-1 flex items-center gap-2">
            <ShieldCheck className="w-5 h-5" /> {calm ? t.dashboard.calm : t.dashboard.threatsUnread(threatsDetected, unreadAlerts)}
          </p>
          <p className="text-white/70 text-sm mt-1">
            {totalAnalyses === 0 ? t.dashboard.empty : t.dashboard.totalAnalyses(totalAnalyses)}
          </p>
        </div>
        {threatsDetected > 0 && <RiskBadge level="DANGEROUS" className="!bg-white/15 !text-white !border-white/25" />}
      </div>
      <div className="px-6 py-4 flex flex-wrap items-center gap-2 bg-white">
        <Link to="/analysis" className="btn-primary !px-5 !py-2 text-sm">
          {t.dashboard.qa1t} <ArrowRight className="w-4 h-4" />
        </Link>
        {unreadAlerts > 0 && (
          <Link to="/alerts" className="btn-secondary !px-5 !py-2 text-sm">
            <Bell className="w-4 h-4" /> {t.alerts.title} ({unreadAlerts})
          </Link>
        )}
      </div>
    </div>
  )
}
