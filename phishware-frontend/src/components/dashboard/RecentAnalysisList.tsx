import { Link } from 'react-router-dom'
import { UrlAnalysisResponse } from '@/types/analysis.types'
import RiskBadge from '@/components/common/RiskBadge'
import { History } from 'lucide-react'
import { useLang } from '@/i18n/LanguageContext'

function formatDate(iso: string, locale: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString(locale, { dateStyle: 'medium', timeStyle: 'short' })
}

export default function RecentAnalysisList({ items }: { items: UrlAnalysisResponse[] }) {
  const { t, lang } = useLang()
  if (items.length === 0) {
    return (
      <div className="card text-center py-10">
        <p className="font-medium text-ink-900">{t.dashboard.recentEmpty}</p>
        <p className="text-sm text-ink-500 mt-1">{t.dashboard.recentEmptySub}</p>
        <Link to="/analysis" className="btn-primary mt-4 !px-5 !py-2 text-sm">
          {t.dashboard.firstAnalysis}
        </Link>
      </div>
    )
  }

  return (
    <div className="card !p-0 overflow-hidden">
      <div className="px-6 py-4 border-b divider flex items-center justify-between">
        <h2 className="font-semibold text-ink-900 flex items-center gap-2">
          <History className="w-4 h-4 text-primary-700" /> {t.dashboard.recent}
        </h2>
        <Link to="/history" className="text-sm font-semibold text-primary-700 hover:text-primary-800">
          {t.dashboard.history}
        </Link>
      </div>
      <ul className="divide-y divide-ink-100">
        {items.slice(0, 5).map((a) => (
          <li key={a.id} className="px-6 py-3.5 flex items-center gap-3 hover:bg-ink-50 transition-colors">
            <div className="flex-1 min-w-0">
              <p className="text-sm font-mono text-ink-900 truncate" title={a.originalUrl}>
                {a.originalUrl}
              </p>
              <p className="text-xs text-ink-500 mt-0.5">
                {a.domain} · <time dateTime={a.analyzedAt}>{formatDate(a.analyzedAt, lang === 'es' ? 'es-CO' : 'en-US')}</time>
              </p>
            </div>
            <RiskBadge level={a.riskLevel} />
          </li>
        ))}
      </ul>
    </div>
  )
}
