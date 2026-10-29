import { useState } from 'react'
import { Check, ChevronDown, Copy, ListTree, RotateCcw } from 'lucide-react'
import clsx from 'clsx'
import { UrlAnalysisResponse } from '@/types/analysis.types'
import ThreatVerdict from './ThreatVerdict'
import ThreatEvidenceList from './ThreatEvidenceList'
import ScoreBreakdown from './ScoreBreakdown'
import { useLang } from '@/i18n/LanguageContext'

interface Props {
  result: UrlAnalysisResponse
  onAnalyzeAnother?: () => void
}

export default function AnalysisResult({ result, onAnalyzeAnother }: Props) {
  const { t } = useLang()
  const [showDetails, setShowDetails] = useState(false)
  const [copied, setCopied] = useState(false)

  // Solo proveedores presentes en ESTA respuesta (nunca inventar resultados).
  const providersInResponse = new Set<string>()
  result.threats.forEach((t) => providersInResponse.add(t.source))
  if (result.scoreBreakdown) Object.keys(result.scoreBreakdown).forEach((k) => providersInResponse.add(k))

  async function copyResult() {
    const text = `PHISHWARE — ${result.riskLevel}\nURL: ${result.originalUrl}\nDominio: ${result.domain}\nRiesgo: ${result.riskScore.toFixed(1)}/100\nMensaje: ${result.riskMessage}`
    try {
      await navigator.clipboard.writeText(text)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    } catch {
      setCopied(false)
    }
  }

  return (
    <div className="space-y-4 animate-fade-in">
      <ThreatVerdict
        level={result.riskLevel}
        message={result.riskMessage}
        url={result.originalUrl}
        domain={result.domain}
        score={result.riskScore}
        analysisTimeMs={result.analysisTimeMs}
        analyzedAt={result.analyzedAt}
      />

      <div className="card">
        <div className="flex justify-between text-xs text-ink-500 mb-1.5">
          <span className="font-medium">{t.analysis.fromBackend}</span>
          <span className="font-bold text-ink-900 tabular-nums">{t.analysis.scoreOut(result.riskScore.toFixed(1))}</span>
        </div>
        <div className="h-2.5 bg-ink-100 rounded-full overflow-hidden" role="progressbar" aria-valuenow={Math.round(result.riskScore)} aria-valuemin={0} aria-valuemax={100} aria-label="Puntuación de riesgo">
          <div
            className={clsx(
              'h-full rounded-full transition-all duration-700',
              result.riskLevel === 'SAFE' ? 'bg-success-500' : result.riskLevel === 'SUSPICIOUS' ? 'bg-warning-500' : 'bg-danger-500',
            )}
            style={{ width: `${Math.min(Math.max(result.riskScore, 0), 100)}%` }}
          />
        </div>
        <ScoreBreakdown breakdown={result.scoreBreakdown} />
      </div>

      <ThreatEvidenceList threats={result.threats} />

      {result.recommendations.length > 0 && (
        <section className="card" aria-label={t.analysis.nextSteps}>
          <h3 className="font-semibold text-ink-900 mb-1">{t.analysis.nextSteps}</h3>
          <ul className="space-y-1.5 mt-2">
            {result.recommendations.map((rec, i) => (
              <li key={i} className="flex items-start gap-2.5 text-sm text-ink-700">
                <span className="mt-[7px] w-1.5 h-1.5 rounded-full bg-primary-600 shrink-0" aria-hidden />
                {rec}
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="card" aria-label="Detalles técnicos">
        <button
          type="button"
          onClick={() => setShowDetails((v) => !v)}
          aria-expanded={showDetails}
          className="w-full flex items-center justify-between gap-2 font-semibold text-ink-900 text-sm"
        >
          <span className="flex items-center gap-2">
            <ListTree className="w-4 h-4 text-primary-700" /> {t.analysis.techDetails}
          </span>
          <ChevronDown className={clsx('w-4 h-4 transition-transform', showDetails && 'rotate-180')} aria-hidden />
        </button>
        {showDetails && (
          <dl className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
            {[
              [t.analysis.dSource, result.analysisSource],
              [t.analysis.dUuid, result.uuid],
              [t.analysis.dPhishing, result.isPhishing ? t.analysis.dYes : t.analysis.dNo],
              [t.analysis.dProviders, providersInResponse.size > 0 ? [...providersInResponse].join(', ') : t.analysis.dNone],
            ].map(([k, v]) => (
              <div key={k} className="rounded-xl border divider px-3.5 py-2.5 bg-ink-50">
                <dt className="text-[11px] font-semibold text-ink-500 uppercase tracking-wide">{k}</dt>
                <dd className="text-ink-900 font-mono text-[13px] break-all mt-0.5">{v}</dd>
              </div>
            ))}
          </dl>
        )}
      </section>

      <div className="flex flex-wrap gap-2">
        {onAnalyzeAnother && (
          <button type="button" onClick={onAnalyzeAnother} className="btn-secondary text-sm">
            <RotateCcw className="w-4 h-4" /> {t.analysis.another}
          </button>
        )}
        <button
          type="button"
          onClick={copyResult}
          className="btn-ghost border divider text-sm"
          aria-live="polite"
        >
          {copied ? <Check className="w-4 h-4 text-success-500" /> : <Copy className="w-4 h-4" />}
          {copied ? t.analysis.copied : t.analysis.copy}
        </button>
      </div>
    </div>
  )
}
