import { ShieldAlert, XCircle, GraduationCap } from 'lucide-react'
import { Link } from 'react-router-dom'
import { ThreatResponse } from '@/types/analysis.types'
import { useLang } from '@/i18n/LanguageContext'

const severityOrder: Record<string, number> = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 }

export default function ThreatEvidenceList({ threats }: { threats: ThreatResponse[] }) {
  const { t } = useLang()
  if (threats.length === 0) {
    return (
      <section className="card" aria-label={t.analysis.evidence}>
        <h3 className="font-semibold text-ink-900 dark:text-ink-50 mb-1">{t.analysis.evidence}</h3>
        <p className="text-sm text-ink-500 dark:text-ink-300">{t.analysis.evidenceNone}</p>
      </section>
    )
  }

  const labels = t.analysis.sources as Record<string, string>
  const principles = t.analysis.principles as Record<string, string>
  const sorted = [...threats].sort((a, b) => (severityOrder[a.severity] ?? 4) - (severityOrder[b.severity] ?? 4))

  return (
    <section className="card" aria-label={`${t.analysis.evidence}: ${threats.length}`}>
      <h3 className="font-semibold text-ink-900 dark:text-ink-50 mb-1 flex items-center gap-2">
        <ShieldAlert className="w-4 h-4 text-danger-500" />
        {t.analysis.evidence} ({threats.length})
      </h3>
      <p className="text-xs text-ink-500 dark:text-ink-300 mb-4">{t.analysis.evidenceSub}</p>
      <ul className="space-y-2.5">
        {sorted.map((threat) => (
          <li key={threat.id} className="flex items-start gap-3 p-3.5 rounded-xl border border-danger-500/15 bg-danger-500/[0.04]">
            <XCircle className="w-4 h-4 text-danger-500 mt-0.5 shrink-0" aria-hidden />
            <div className="min-w-0">
              <p className="text-sm font-semibold text-ink-900 dark:text-ink-50">{threat.threatType}</p>
              <p className="text-sm text-ink-500 dark:text-ink-300 mt-0.5">{threat.description}</p>
              <p className="text-xs text-ink-500 dark:text-ink-300 mt-1.5">
                {t.analysis.dSource}: {labels[threat.source] ?? threat.source} · {t.alerts.sev}: {threat.severity}
              </p>
              {threat.persuasionPrinciple && threat.persuasionPrinciple !== 'NONE' && (
                <p className="text-xs text-ink-500 dark:text-ink-300 mt-1.5 flex flex-wrap items-center gap-1">
                  {t.analysis.principle}: {principles[threat.persuasionPrinciple] ?? threat.persuasionPrinciple}
                  {threat.microlessonSlug && (
                    <Link
                      to={`/education/${threat.microlessonSlug}`}
                      className="inline-flex items-center gap-1 font-semibold text-primary-700 hover:underline"
                    >
                      <GraduationCap className="w-3.5 h-3.5" aria-hidden />
                      {t.analysis.viewLesson}
                    </Link>
                  )}
                </p>
              )}
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
