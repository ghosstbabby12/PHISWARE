import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { CheckCircle2, Circle, CircleDashed, GraduationCap, TrendingUp } from 'lucide-react'
import { educationService } from '@/services/educationService'
import { ModuleProgress } from '@/types/education.types'
import { useLang } from '@/i18n/LanguageContext'

const STATUS_CLASS: Record<ModuleProgress['status'], string> = {
  PASSED:      'text-success-600',
  IN_PROGRESS: 'text-warning-600',
  NOT_STARTED: 'text-ink-500 dark:text-ink-300',
}

function StatusIcon({ status }: { status: ModuleProgress['status'] }) {
  if (status === 'PASSED') return <CheckCircle2 className="w-4 h-4 text-success-600 flex-shrink-0" />
  if (status === 'IN_PROGRESS') return <CircleDashed className="w-4 h-4 text-warning-600 flex-shrink-0" />
  return <Circle className="w-4 h-4 text-ink-300 dark:text-ink-500 flex-shrink-0" />
}

export function useLearningProgress() {
  const { lang } = useLang()
  return useQuery({
    queryKey: ['quiz-progress', lang],
    queryFn: educationService.getProgress,
  })
}

export default function LearningProgressPanel() {
  const { data, isLoading } = useLearningProgress()
  const { t } = useLang()
  const p = t.edu.progress

  if (isLoading) return <div className="card animate-pulse h-48" />
  if (!data || data.totalModules === 0) return null

  return (
    <section className="card space-y-5" aria-labelledby="learning-progress-title">
      <div className="flex flex-col md:flex-row md:items-center gap-4 justify-between">
        <div>
          <h2 id="learning-progress-title" className="text-lg font-semibold text-ink-900 dark:text-ink-50 flex items-center gap-2">
            <GraduationCap className="w-5 h-5 text-primary-700" />
            {p.title}
          </h2>
          <p className="text-sm text-ink-500 dark:text-ink-300 mt-1">
            {p.subtitle}
          </p>
        </div>
        <div className="grid grid-cols-3 gap-3 text-center md:min-w-96">
          <div className="rounded-lg bg-ink-50 dark:bg-dark-900 border divider p-3">
            <p className="text-xl font-bold text-ink-900 dark:text-ink-50">{data.modulesPassed}/{data.totalModules}</p>
            <p className="text-xs text-ink-500 dark:text-ink-300">{p.passedModules}</p>
          </div>
          <div className="rounded-lg bg-ink-50 dark:bg-dark-900 border divider p-3">
            <p className="text-xl font-bold text-ink-900 dark:text-ink-50">{data.averageBestScore ?? '—'}{data.averageBestScore != null && '%'}</p>
            <p className="text-xs text-ink-500 dark:text-ink-300">{p.avgScore}</p>
          </div>
          <div className="rounded-lg bg-ink-50 dark:bg-dark-900 border divider p-3">
            <p className="text-xl font-bold text-ink-900 dark:text-ink-50">{data.totalAttempts}</p>
            <p className="text-xs text-ink-500 dark:text-ink-300">{p.attempts}</p>
          </div>
        </div>
      </div>

      <div>
        <div className="flex justify-between text-xs text-ink-500 dark:text-ink-300 mb-1.5">
          <span className="flex items-center gap-1"><TrendingUp className="w-3.5 h-3.5" /> {p.overall}</span>
          <span>{data.completionPercentage}%</span>
        </div>
        <div className="h-2 rounded-full bg-ink-100 dark:bg-dark-800 overflow-hidden">
          <div className="h-full bg-success-500 transition-all" style={{ width: `${data.completionPercentage}%` }} />
        </div>
      </div>

      <ul className="grid grid-cols-1 md:grid-cols-2 gap-2">
        {data.modules.map(m => (
          <li key={m.quizId}>
            <Link
              to={`/education/${m.slug}#quiz`}
              className="flex items-center gap-3 rounded-lg border border-ink-200 dark:border-dark-800 px-3 py-2.5 hover:border-primary-600 transition-colors"
            >
              <StatusIcon status={m.status} />
              <span className="flex-1 min-w-0">
                <span className="block text-sm text-ink-900 dark:text-ink-50 truncate">{m.title}</span>
                <span className={`block text-xs ${STATUS_CLASS[m.status]}`}>
                  {p.status[m.status]}
                  {m.attempts > 0 && p.best(m.bestScore, m.attempts)}
                </span>
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  )
}
