import { ShieldAlert, XCircle } from 'lucide-react'
import { ThreatResponse } from '@/types/analysis.types'
import { useLang } from '@/i18n/LanguageContext'

const sourceLabels: Record<string, Record<string, string>> = {
  es: {
    GOOGLE_SAFE_BROWSING: 'Google Safe Browsing',
    VIRUS_TOTAL: 'VirusTotal',
    HEURISTIC_NIST: 'Reglas de URL',
    CONTENT_SCRAPING: 'Contenido de la página',
    DOMAIN_INTEL: 'Antigüedad del dominio',
  },
  en: {
    GOOGLE_SAFE_BROWSING: 'Google Safe Browsing',
    VIRUS_TOTAL: 'VirusTotal',
    HEURISTIC_NIST: 'URL rules',
    CONTENT_SCRAPING: 'Page content',
    DOMAIN_INTEL: 'Domain age',
  },
}

const severityOrder: Record<string, number> = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 }

export default function ThreatEvidenceList({ threats }: { threats: ThreatResponse[] }) {
  const { t, lang } = useLang()
  if (threats.length === 0) {
    return (
      <section className="card" aria-label={t.analysis.evidence}>
        <h3 className="font-semibold text-ink-900 mb-1">{t.analysis.evidence}</h3>
        <p className="text-sm text-ink-500">{t.analysis.evidenceNone}</p>
      </section>
    )
  }

  const labels = sourceLabels[lang]
  const sorted = [...threats].sort((a, b) => (severityOrder[a.severity] ?? 4) - (severityOrder[b.severity] ?? 4))

  return (
    <section className="card" aria-label={`${t.analysis.evidence}: ${threats.length}`}>
      <h3 className="font-semibold text-ink-900 mb-1 flex items-center gap-2">
        <ShieldAlert className="w-4 h-4 text-danger-500" />
        {t.analysis.evidence} ({threats.length})
      </h3>
      <p className="text-xs text-ink-500 mb-4">{t.analysis.evidenceSub}</p>
      <ul className="space-y-2.5">
        {sorted.map((threat) => (
          <li key={threat.id} className="flex items-start gap-3 p-3.5 rounded-xl border border-danger-500/15 bg-danger-500/[0.04]">
            <XCircle className="w-4 h-4 text-danger-500 mt-0.5 shrink-0" aria-hidden />
            <div className="min-w-0">
              <p className="text-sm font-semibold text-ink-900">{threat.threatType}</p>
              <p className="text-sm text-ink-500 mt-0.5">{threat.description}</p>
              <p className="text-xs text-ink-500 mt-1.5">
                {lang === 'es' ? 'Fuente' : 'Source'}: {labels[threat.source] ?? threat.source} · {t.alerts.sev}: {threat.severity}
              </p>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
