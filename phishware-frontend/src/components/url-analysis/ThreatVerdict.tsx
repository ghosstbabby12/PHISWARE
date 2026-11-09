import { AlertTriangle, CheckCircle, ShieldCheck, XCircle } from 'lucide-react'
import clsx from 'clsx'
import { OfficialSeal, RiskLevel } from '@/types/analysis.types'
import RiskBadge from '@/components/common/RiskBadge'
import { useLang } from '@/i18n/LanguageContext'

export default function ThreatVerdict({
  level,
  message,
  url,
  domain,
  score,
  analysisTimeMs,
  analyzedAt,
  officialSeal,
}: {
  level: RiskLevel
  message: string
  url: string
  domain: string
  score: number
  analysisTimeMs: number
  analyzedAt: string
  officialSeal?: OfficialSeal | null
}) {
  const { t, lang } = useLang()
  const Icon = level === 'SAFE' ? CheckCircle : level === 'SUSPICIOUS' ? AlertTriangle : XCircle
  const card = level === 'SAFE'
    ? 'border-success-500/40 bg-success-500/[0.06]'
    : level === 'SUSPICIOUS'
      ? 'border-warning-500/40 bg-warning-500/[0.07]'
      : 'border-danger-500/40 bg-danger-500/[0.06]'
  const note = level === 'SAFE' ? t.analysis.safeNote : level === 'SUSPICIOUS' ? t.analysis.suspNote : t.analysis.dangNote
  const date = new Date(analyzedAt)
  const when = Number.isNaN(date.getTime())
    ? analyzedAt
    : date.toLocaleString(lang === 'es' ? 'es-CO' : 'en-US', { dateStyle: 'medium', timeStyle: 'short' })

  return (
    <section aria-live="polite" aria-label={`${t.analysis.verdict[level]}: ${score.toFixed(0)}/100`} className={clsx('card border-2', card)}>
      <div className="flex items-start gap-4">
        <Icon
          className={clsx(
            'w-10 h-10 shrink-0',
            level === 'SAFE' ? 'text-success-500' : level === 'SUSPICIOUS' ? 'text-warning-500' : 'text-danger-500',
          )}
          aria-hidden
        />
        <div className="flex-1 min-w-0">
          <div className="flex flex-wrap items-center gap-2.5">
            <h2 className="text-lg font-bold text-ink-900 dark:text-ink-50">{message}</h2>
            <RiskBadge level={level} />
            {officialSeal?.official && (
              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-success-500/10 text-success-600 border border-success-500/25">
                <ShieldCheck className="w-3.5 h-3.5" aria-hidden />
                {t.analysis.sealOfficial(officialSeal.brand ?? '')}
              </span>
            )}
          </div>
          <p className="text-sm text-ink-500 dark:text-ink-300 font-mono break-all mt-1">{url}</p>
          <dl className="flex flex-wrap gap-x-5 gap-y-1 mt-2 text-xs text-ink-500 dark:text-ink-300">
            <div className="flex gap-1.5"><dt className="font-semibold">{t.analysis.domain}:</dt><dd className="font-mono">{domain}</dd></div>
            <div className="flex gap-1.5"><dt className="font-semibold">{t.analysis.analyzedAt}:</dt><dd><time dateTime={analyzedAt}>{when}</time> · {t.analysis.analyzedIn(analysisTimeMs)}</dd></div>
          </dl>
        </div>
        <div className="text-right shrink-0" aria-hidden>
          <p className="text-3xl font-bold text-ink-900 dark:text-ink-50 tabular-nums">{score.toFixed(0)}</p>
          <p className="text-[11px] text-ink-500 dark:text-ink-300 font-medium">/ 100</p>
        </div>
      </div>
      <p className="text-sm text-ink-700 dark:text-ink-200 bg-white/70 dark:bg-dark-800/70 border divider rounded-xl px-4 py-3 mt-4 leading-relaxed">
        {note}
      </p>
    </section>
  )
}
