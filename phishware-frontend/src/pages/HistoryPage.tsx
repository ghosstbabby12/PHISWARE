import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { History } from 'lucide-react'
import { urlAnalysisService } from '@/services/urlAnalysisService'
import RiskBadge from '@/components/common/RiskBadge'
import AppHeader from '@/components/common/AppHeader'
import { EmptyState, ErrorState, LoadingState } from '@/components/common/FeedbackStates'
import { useLang } from '@/i18n/LanguageContext'
import { RiskLevel } from '@/types/analysis.types'

function formatDate(iso: string, locale: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString(locale, { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

export default function HistoryPage() {
  const { t, lang } = useLang()
  const [page, setPage] = useState(0)
  const [riskFilter, setRiskFilter] = useState('')

  const RISK_FILTERS = [
    { value: '', label: t.history.all },
    { value: 'SAFE', label: t.history.safe },
    { value: 'SUSPICIOUS', label: t.history.susp },
    { value: 'DANGEROUS', label: t.history.dang },
  ]

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['history', page, riskFilter],
    queryFn: () => urlAnalysisService.getHistory(page, 10, riskFilter || undefined),
  })

  return (
    <div className="space-y-6 animate-fade-in">
      <AppHeader
        title={t.history.title}
        subtitle={t.history.sub(data?.totalElements ?? 0)}
        actions={
          <div className="flex items-center gap-1.5 bg-white border divider rounded-xl p-1 shadow-sm" role="group" aria-label={t.history.filter}>
            {RISK_FILTERS.map(f => (
              <button
                key={f.value}
                onClick={() => { setRiskFilter(f.value); setPage(0) }}
                aria-pressed={riskFilter === f.value}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                  riskFilter === f.value
                    ? 'bg-primary-600 text-white shadow-sm'
                    : 'text-ink-500 hover:text-ink-900 hover:bg-ink-50'
                }`}
              >
                {f.label}
              </button>
            ))}
          </div>
        }
      />

      {isLoading ? (
        <LoadingState message={t.history.load} rows={5} />
      ) : isError ? (
        <ErrorState
          title={t.history.loadFail}
          description={t.history.loadFailSub}
          onRetry={() => refetch()}
        />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          icon={History}
          title={t.history.empty}
          description={riskFilter ? t.history.emptyFilter : t.history.emptyCta}
          action={<Link to="/analysis" className="btn-primary text-sm">{t.history.emptyCta}</Link>}
        />
      ) : (
        <div className="card !p-0 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px]">
              <caption className="sr-only">{t.history.title}</caption>
              <thead>
                <tr className="border-b divider bg-ink-50/60">
                  <th scope="col" className="text-left text-xs font-semibold text-ink-500 uppercase tracking-wider px-6 py-3">{t.history.url}</th>
                  <th scope="col" className="text-left text-xs font-semibold text-ink-500 uppercase tracking-wider px-4 py-3">{t.history.domain}</th>
                  <th scope="col" className="text-left text-xs font-semibold text-ink-500 uppercase tracking-wider px-4 py-3">{t.history.risk}</th>
                  <th scope="col" className="text-left text-xs font-semibold text-ink-500 uppercase tracking-wider px-4 py-3">{t.history.score}</th>
                  <th scope="col" className="text-left text-xs font-semibold text-ink-500 uppercase tracking-wider px-4 py-3">{t.history.date}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100">
                {data.content.map((item) => (
                  <tr key={item.id} className="hover:bg-primary-50/40 transition-colors">
                    <td className="px-6 py-4">
                      <p className="text-sm font-mono text-ink-900 truncate max-w-xs" title={item.originalUrl}>
                        {item.originalUrl}
                      </p>
                    </td>
                    <td className="px-4 py-4 text-sm text-ink-500 font-mono">{item.domain}</td>
                    <td className="px-4 py-4">
                      <RiskBadge level={item.riskLevel as RiskLevel} />
                    </td>
                    <td className="px-4 py-4 text-sm font-mono font-semibold text-ink-900 tabular-nums">
                      {item.riskScore?.toFixed(1)}
                    </td>
                    <td className="px-4 py-4 text-sm text-ink-500 whitespace-nowrap">
                      <time dateTime={item.analyzedAt}>{formatDate(item.analyzedAt, lang === 'es' ? 'es-CO' : 'en-US')}</time>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {data && data.totalPages > 1 && (
        <nav className="flex items-center justify-center gap-2" aria-label={t.history.title}>
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={data.first}
            className="btn-ghost border divider disabled:opacity-40"
          >
            {t.history.prev}
          </button>
          <span className="text-sm text-ink-500 tabular-nums" aria-live="polite">
            {t.history.page(data.number + 1, data.totalPages)}
          </span>
          <button
            onClick={() => setPage(p => p + 1)}
            disabled={data.last}
            className="btn-ghost border divider disabled:opacity-40"
          >
            {t.history.next}
          </button>
        </nav>
      )}

      {!isLoading && !isError && (data?.content.length ?? 0) > 0 && (
        <p className="text-xs text-ink-500">{t.history.note}</p>
      )}
    </div>
  )
}
