import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { CheckCircle2, Circle, CircleDashed, GraduationCap, TrendingUp } from 'lucide-react'
import { educationService } from '@/services/educationService'
import { ModuleProgress } from '@/types/education.types'

const STATUS_STYLE: Record<ModuleProgress['status'], { label: string; className: string }> = {
  PASSED:      { label: 'Aprobado',    className: 'text-success-600' },
  IN_PROGRESS: { label: 'Por aprobar', className: 'text-warning-600' },
  NOT_STARTED: { label: 'Sin empezar', className: 'text-ink-500' },
}

function StatusIcon({ status }: { status: ModuleProgress['status'] }) {
  if (status === 'PASSED') return <CheckCircle2 className="w-4 h-4 text-success-600 flex-shrink-0" />
  if (status === 'IN_PROGRESS') return <CircleDashed className="w-4 h-4 text-warning-600 flex-shrink-0" />
  return <Circle className="w-4 h-4 text-ink-300 flex-shrink-0" />
}

export function useLearningProgress() {
  return useQuery({
    queryKey: ['quiz-progress'],
    queryFn: educationService.getProgress,
  })
}

export default function LearningProgressPanel() {
  const { data, isLoading } = useLearningProgress()

  if (isLoading) return <div className="card animate-pulse h-48" />
  if (!data || data.totalModules === 0) return null

  return (
    <section className="card space-y-5" aria-labelledby="learning-progress-title">
      <div className="flex flex-col md:flex-row md:items-center gap-4 justify-between">
        <div>
          <h2 id="learning-progress-title" className="text-lg font-semibold text-ink-900 flex items-center gap-2">
            <GraduationCap className="w-5 h-5 text-primary-700" />
            Tu progreso de aprendizaje
          </h2>
          <p className="text-sm text-ink-500 mt-1">
            Aprueba el quiz de cada módulo para completarlo.
          </p>
        </div>
        <div className="grid grid-cols-3 gap-3 text-center md:min-w-96">
          <div className="rounded-lg bg-ink-50 border divider p-3">
            <p className="text-xl font-bold text-ink-900">{data.modulesPassed}/{data.totalModules}</p>
            <p className="text-xs text-ink-500">módulos aprobados</p>
          </div>
          <div className="rounded-lg bg-ink-50 border divider p-3">
            <p className="text-xl font-bold text-ink-900">{data.averageBestScore ?? '—'}{data.averageBestScore != null && '%'}</p>
            <p className="text-xs text-ink-500">puntaje promedio</p>
          </div>
          <div className="rounded-lg bg-ink-50 border divider p-3">
            <p className="text-xl font-bold text-ink-900">{data.totalAttempts}</p>
            <p className="text-xs text-ink-500">intentos</p>
          </div>
        </div>
      </div>

      <div>
        <div className="flex justify-between text-xs text-ink-500 mb-1.5">
          <span className="flex items-center gap-1"><TrendingUp className="w-3.5 h-3.5" /> Avance general</span>
          <span>{data.completionPercentage}%</span>
        </div>
        <div className="h-2 rounded-full bg-ink-100 overflow-hidden">
          <div className="h-full bg-success-500 transition-all" style={{ width: `${data.completionPercentage}%` }} />
        </div>
      </div>

      <ul className="grid grid-cols-1 md:grid-cols-2 gap-2">
        {data.modules.map(m => (
          <li key={m.quizId}>
            <Link
              to={`/education/${m.slug}#quiz`}
              className="flex items-center gap-3 rounded-lg border border-ink-200 px-3 py-2.5 hover:border-primary-600 transition-colors"
            >
              <StatusIcon status={m.status} />
              <span className="flex-1 min-w-0">
                <span className="block text-sm text-ink-900 truncate">{m.title}</span>
                <span className={`block text-xs ${STATUS_STYLE[m.status].className}`}>
                  {STATUS_STYLE[m.status].label}
                  {m.attempts > 0 && ` · mejor ${m.bestScore}% · ${m.attempts} ${m.attempts === 1 ? 'intento' : 'intentos'}`}
                </span>
              </span>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  )
}
