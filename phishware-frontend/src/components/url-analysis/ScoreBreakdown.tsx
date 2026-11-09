import clsx from 'clsx'
import { useLang } from '@/i18n/LanguageContext'

interface Props {
  breakdown?: Record<string, number>
}

const sourceLabels = (t: { analysis: { sources: Record<string, string> } }) => t.analysis.sources

function barColor(value: number): string {
  if (value >= 70) return 'bg-danger-500'
  if (value >= 30) return 'bg-warning-500'
  return 'bg-success-500'
}

/** Aporte por fuente SOLO si el backend lo incluyó en esta respuesta. */
export default function ScoreBreakdown({ breakdown }: Props) {
  const { t } = useLang()
  if (!breakdown || Object.keys(breakdown).length === 0) return null

  const labels = sourceLabels(t)
  const entries = Object.entries(breakdown).sort((a, b) => b[1] - a[1])

  return (
    <div className="mt-5">
      <h4 className="text-sm font-semibold text-ink-900 dark:text-ink-50">{t.analysis.fromBreakdown}</h4>
      <p className="text-xs text-ink-500 dark:text-ink-300 mb-3 mt-0.5">{t.analysis.fromBreakdownSub}</p>
      <div className="space-y-2.5">
        {entries.map(([source, value]) => (
          <div key={source}>
            <div className="flex justify-between text-xs text-ink-500 dark:text-ink-300 mb-1">
              <span>{labels[source] ?? source}</span>
              <span className="font-semibold text-ink-900 dark:text-ink-50 tabular-nums">{Number(value).toFixed(0)} / 100</span>
            </div>
            <div className="h-1.5 bg-ink-100 dark:bg-dark-800 rounded-full overflow-hidden">
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
