import clsx from 'clsx'
import { useLang } from '@/i18n/LanguageContext'

interface Props {
  breakdown?: Record<string, number>
}

const sourceLabels: Record<string, Record<string, string>> = {
  es: {
    googleSafeBrowsing: 'Google Safe Browsing',
    GOOGLE_SAFE_BROWSING: 'Google Safe Browsing',
    virusTotal: 'VirusTotal',
    VIRUS_TOTAL: 'VirusTotal',
    urlHeuristic: 'Reglas de URL',
    HEURISTIC_NIST: 'Reglas de URL',
    pageContent: 'Contenido de la página',
    CONTENT_SCRAPING: 'Contenido de la página',
    domainAge: 'Antigüedad del dominio',
    DOMAIN_INTEL: 'Antigüedad del dominio',
  },
  en: {
    googleSafeBrowsing: 'Google Safe Browsing',
    GOOGLE_SAFE_BROWSING: 'Google Safe Browsing',
    virusTotal: 'VirusTotal',
    VIRUS_TOTAL: 'VirusTotal',
    urlHeuristic: 'URL rules',
    HEURISTIC_NIST: 'URL rules',
    pageContent: 'Page content',
    CONTENT_SCRAPING: 'Page content',
    domainAge: 'Domain age',
    DOMAIN_INTEL: 'Domain age',
  },
}

function barColor(value: number): string {
  if (value >= 70) return 'bg-danger-500'
  if (value >= 30) return 'bg-warning-500'
  return 'bg-success-500'
}

/** Aporte por fuente SOLO si el backend lo incluyó en esta respuesta. */
export default function ScoreBreakdown({ breakdown }: Props) {
  const { t, lang } = useLang()
  if (!breakdown || Object.keys(breakdown).length === 0) return null

  const labels = sourceLabels[lang]
  const entries = Object.entries(breakdown).sort((a, b) => b[1] - a[1])

  return (
    <div className="mt-5">
      <h4 className="text-sm font-semibold text-ink-900">{t.analysis.fromBreakdown}</h4>
      <p className="text-xs text-ink-500 mb-3 mt-0.5">{t.analysis.fromBreakdownSub}</p>
      <div className="space-y-2.5">
        {entries.map(([source, value]) => (
          <div key={source}>
            <div className="flex justify-between text-xs text-ink-500 mb-1">
              <span>{labels[source] ?? source}</span>
              <span className="font-semibold text-ink-900 tabular-nums">{Number(value).toFixed(0)} / 100</span>
            </div>
            <div className="h-1.5 bg-ink-100 rounded-full overflow-hidden">
              <div
                className={clsx('h-full rounded-full', barColor(Number(value)))}
                style={{ width: `${Math.min(Math.max(Number(value) || 0, 0), 100)}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
