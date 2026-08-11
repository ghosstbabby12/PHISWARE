import { UrlAnalysisResponse } from '@/types/analysis.types'
import RiskBadge from '@/components/common/RiskBadge'
import { AlertTriangle, CheckCircle, Clock, Globe, Shield, XCircle } from 'lucide-react'
import clsx from 'clsx'

interface Props {
  result: UrlAnalysisResponse
}

export default function AnalysisResult({ result }: Props) {
  const riskColorMap = {
    SAFE:       'border-success-500/30 bg-success-500/5',
    SUSPICIOUS: 'border-warning-500/30 bg-warning-500/5',
    DANGEROUS:  'border-danger-500/30 bg-danger-500/5',
  }

  const riskIconMap = {
    SAFE:       <CheckCircle className="w-10 h-10 text-success-500" />,
    SUSPICIOUS: <AlertTriangle className="w-10 h-10 text-warning-500" />,
    DANGEROUS:  <XCircle className="w-10 h-10 text-danger-500" />,
  }

  return (
    <div className={clsx('card border-2 animate-fade-in', riskColorMap[result.riskLevel])}>
      {/* Header */}
      <div className="flex items-start gap-4 mb-6">
        {riskIconMap[result.riskLevel]}
        <div className="flex-1">
          <div className="flex items-center gap-3 mb-1">
            <h3 className="text-lg font-bold text-white">{result.riskMessage}</h3>
            <RiskBadge level={result.riskLevel} />
          </div>
          <p className="text-sm text-slate-400 font-mono break-all">{result.originalUrl}</p>
        </div>
        <div className="text-right text-xs text-slate-500">
          <div className="flex items-center gap-1.5 justify-end">
            <Clock className="w-3 h-3" />
            {result.analysisTimeMs}ms
          </div>
          <div className="flex items-center gap-1.5 justify-end mt-1">
            <Globe className="w-3 h-3" />
            {result.domain}
          </div>
        </div>
      </div>

      {/* Score bar */}
      <div className="mb-6">
        <div className="flex justify-between text-xs text-slate-400 mb-1.5">
          <span>Puntuación de Riesgo</span>
          <span className="font-bold">{result.riskScore.toFixed(1)} / 100</span>
        </div>
        <div className="h-2.5 bg-dark-900 rounded-full overflow-hidden">
          <div
            className={clsx(
              'h-full rounded-full transition-all duration-700',
              result.riskLevel === 'SAFE'       ? 'bg-success-500' :
              result.riskLevel === 'SUSPICIOUS'  ? 'bg-warning-500' : 'bg-danger-500'
            )}
            style={{ width: `${Math.min(result.riskScore, 100)}%` }}
          />
        </div>
      </div>

      {/* Threats */}
      {result.threats.length > 0 && (
        <div className="mb-6">
          <h4 className="text-sm font-semibold text-white mb-3 flex items-center gap-2">
            <Shield className="w-4 h-4 text-danger-400" />
            Amenazas Detectadas ({result.threats.length})
          </h4>
          <div className="space-y-2">
            {result.threats.map((threat) => (
              <div key={threat.id} className="flex items-start gap-3 p-3 bg-danger-500/10 rounded-lg border border-danger-500/20">
                <XCircle className="w-4 h-4 text-danger-400 mt-0.5 flex-shrink-0" />
                <div>
                  <p className="text-sm font-medium text-danger-300">{threat.threatType}</p>
                  <p className="text-xs text-slate-400">{threat.description}</p>
                  <p className="text-xs text-slate-500 mt-1">Fuente: {threat.source}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Recommendations */}
      {result.recommendations.length > 0 && (
        <div>
          <h4 className="text-sm font-semibold text-white mb-3">Recomendaciones</h4>
          <ul className="space-y-1.5">
            {result.recommendations.map((rec, i) => (
              <li key={i} className="flex items-start gap-2 text-sm text-slate-300">
                <span className="mt-1 w-1.5 h-1.5 rounded-full bg-primary-400 flex-shrink-0" />
                {rec}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
