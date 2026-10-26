import clsx from 'clsx'

interface Props {
  breakdown?: Record<string, number>
}

const sourceLabels: Record<string, string> = {
  googleSafeBrowsing: 'Google Safe Browsing',
  virusTotal: 'VirusTotal',
  urlHeuristic: 'Reglas de URL',
  pageContent: 'Contenido de la página',
  domainAge: 'Antigüedad del dominio',
}

function barColor(value: number): string {
  if (value >= 70) return 'bg-danger-500'
  if (value >= 30) return 'bg-warning-500'
  return 'bg-success-500'
}

export default function ScoreBreakdown({ breakdown }: Props) {
  if (!breakdown || Object.keys(breakdown).length === 0) return null

  const entries = Object.entries(breakdown).sort((a, b) => b[1] - a[1])

  return (
    <div className="mb-6">
      <h4 className="text-sm font-semibold text-white mb-3">De dónde sale el puntaje</h4>
      <div className="space-y-2">
        {entries.map(([source, value]) => (
          <div key={source}>
            <div className="flex justify-between text-xs text-slate-400 mb-1">
              <span>{sourceLabels[source] ?? source}</span>
              <span className="font-mono">{Number(value).toFixed(0)} / 100</span>
            </div>
            <div className="h-1.5 bg-dark-900 rounded-full overflow-hidden">
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
