import { Bar, BarChart, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { useLang } from '@/i18n/LanguageContext'

interface Props {
  breakdown?: Record<string, number>
}

function barColor(value: number): string {
  if (value >= 70) return '#C6283D'
  if (value >= 30) return '#C78316'
  return '#16845B'
}

/** Aporte por fuente SOLO si el backend lo incluyó en esta respuesta (barras con recharts). */
export default function ScoreBreakdown({ breakdown }: Props) {
  const { t } = useLang()
  if (!breakdown || Object.keys(breakdown).length === 0) return null

  const labels = t.analysis.sources as Record<string, string>
  const entries = Object.entries(breakdown)
    .sort((a, b) => b[1] - a[1])
    .map(([source, value]) => ({
      source: labels[source] ?? source,
      value: Number(value) || 0,
    }))

  return (
    <div className="mt-5">
      <h4 className="text-sm font-semibold text-ink-900 dark:text-ink-50">{t.analysis.fromBreakdown}</h4>
      <p className="text-xs text-ink-500 dark:text-ink-300 mb-3 mt-0.5">{t.analysis.fromBreakdownSub}</p>
      <div className="h-56" role="img" aria-label={t.analysis.fromBreakdown}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={entries} layout="vertical" margin={{ top: 0, right: 24, left: 8, bottom: 0 }}>
            <XAxis type="number" domain={[0, 100]} hide />
            <YAxis type="category" dataKey="source" width={120} tick={{ fontSize: 11 }} />
            <Tooltip formatter={(v) => [`${Number(v).toFixed(0)} / 100`, t.analysis.score]} />
            <Bar dataKey="value" radius={[0, 6, 6, 0]} background={{ fill: 'currentColor', opacity: 0.08 }}>
              {entries.map((e, i) => (
                <Cell key={i} fill={barColor(e.value)} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
