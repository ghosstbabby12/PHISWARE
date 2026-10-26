import { useEffect, useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Award, CheckCircle2, ChevronLeft, ChevronRight, ClipboardCheck, Clock, RotateCcw, XCircle } from 'lucide-react'
import toast from 'react-hot-toast'
import { educationService } from '@/services/educationService'
import { getErrorMessage } from '@/services/api'
import { QuizQuestion, QuizResult } from '@/types/education.types'

type Phase = 'intro' | 'answering' | 'result'
type Answers = Record<number, string[]>

function formatTime(sec: number) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

function QuestionCard({ question, selected, onChange }: {
  question: QuizQuestion
  selected: string[]
  onChange: (value: string[]) => void
}) {
  const multiple = question.questionType === 'MULTIPLE_CHOICE'

  function toggle(key: string) {
    if (!multiple) return onChange([key])
    onChange(selected.includes(key) ? selected.filter(k => k !== key) : [...selected, key])
  }

  return (
    <div>
      <p className="text-white font-medium leading-relaxed">{question.questionText}</p>
      {multiple && <p className="text-xs text-slate-500 mt-1">Puedes seleccionar varias opciones</p>}
      <div className="mt-4 space-y-2">
        {Object.entries(question.options).map(([key, label]) => {
          const active = selected.includes(key)
          return (
            <button
              key={key}
              type="button"
              onClick={() => toggle(key)}
              className={`w-full text-left flex items-start gap-3 p-3 rounded-lg border transition-colors ${
                active
                  ? 'border-primary-500 bg-primary-600/15 text-white'
                  : 'border-slate-700 bg-dark-800/50 text-slate-300 hover:border-slate-500'
              }`}
            >
              <span className={`flex-shrink-0 w-6 h-6 flex items-center justify-center text-xs font-bold border ${
                multiple ? 'rounded' : 'rounded-full'
              } ${active ? 'bg-primary-600 border-primary-500 text-white' : 'border-slate-600 text-slate-400'}`}>
                {key}
              </span>
              <span className="text-sm leading-relaxed">{label}</span>
            </button>
          )
        })}
      </div>
    </div>
  )
}

function ResultView({ result, questions, onRetry }: {
  result: QuizResult
  questions: QuizQuestion[]
  onRetry: () => void
}) {
  const byId = new Map(questions.map(q => [q.id, q]))
  return (
    <div className="space-y-5">
      <div className={`rounded-xl p-5 border text-center ${
        result.passed ? 'border-success-500/40 bg-success-500/10' : 'border-warning-500/40 bg-warning-500/10'
      }`}>
        <p className={`text-4xl font-bold ${result.passed ? 'text-success-400' : 'text-warning-400'}`}>{result.score}%</p>
        <p className="text-white font-semibold mt-1">
          {result.passed ? '¡Aprobaste este módulo!' : `Necesitas ${result.passingScore}% para aprobar`}
        </p>
        <p className="text-sm text-slate-400 mt-1">
          {result.correctCount} de {result.totalQuestions} correctas · Intento #{result.attempts} · Mejor puntaje: {result.bestScore}%
        </p>
        {result.pointsEarned > 0 && (
          <p className="inline-flex items-center gap-1.5 mt-3 text-sm font-medium text-primary-300">
            <Award className="w-4 h-4" /> +{result.pointsEarned} puntos
          </p>
        )}
      </div>

      <div className="space-y-3">
        {result.feedback.map((f, i) => {
          const q = byId.get(f.questionId)
          if (!q) return null
          return (
            <div key={f.questionId} className={`rounded-lg border p-4 ${
              f.correct ? 'border-success-500/30 bg-success-500/5' : 'border-danger-500/30 bg-danger-500/5'
            }`}>
              <div className="flex items-start gap-2">
                {f.correct
                  ? <CheckCircle2 className="w-5 h-5 text-success-400 flex-shrink-0 mt-0.5" />
                  : <XCircle className="w-5 h-5 text-danger-400 flex-shrink-0 mt-0.5" />}
                <div className="text-sm">
                  <p className="text-white font-medium">{i + 1}. {q.questionText}</p>
                  {!f.correct && (
                    <p className="text-slate-400 mt-1">
                      Tu respuesta: {f.selected.length ? f.selected.map(k => q.options[k]).join(', ') : 'sin responder'}
                    </p>
                  )}
                  <p className="text-success-400 mt-1">
                    Correcta: {f.correctAnswers.map(k => q.options[k]).join(', ')}
                  </p>
                  {f.explanation && <p className="text-slate-400 mt-2 leading-relaxed">{f.explanation}</p>}
                </div>
              </div>
            </div>
          )
        })}
      </div>

      <button type="button" onClick={onRetry} className="btn-secondary w-full inline-flex items-center justify-center gap-2">
        <RotateCcw className="w-4 h-4" /> Intentar de nuevo
      </button>
    </div>
  )
}

export default function ModuleQuiz({ slug }: { slug: string }) {
  const queryClient = useQueryClient()
  const [phase, setPhase] = useState<Phase>('intro')
  const [current, setCurrent] = useState(0)
  const [answers, setAnswers] = useState<Answers>({})
  const [elapsed, setElapsed] = useState(0)
  const [result, setResult] = useState<QuizResult | null>(null)
  const startedAt = useRef<number>(0)

  const { data: quiz, isLoading, isError } = useQuery({
    queryKey: ['module-quiz', slug],
    queryFn: () => educationService.getModuleQuiz(slug),
    retry: false,
  })

  const submit = useMutation({
    mutationFn: educationService.submitQuiz,
    onSuccess: (data) => {
      setResult(data)
      setPhase('result')
      queryClient.invalidateQueries({ queryKey: ['module-quiz', slug] })
      queryClient.invalidateQueries({ queryKey: ['quiz-progress'] })
      if (data.firstTimePassed) toast.success(`¡Módulo aprobado! +${data.pointsEarned} puntos`)
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  })

  useEffect(() => {
    if (phase !== 'answering') return
    const id = window.setInterval(() => setElapsed(Math.floor((Date.now() - startedAt.current) / 1000)), 1000)
    return () => window.clearInterval(id)
  }, [phase])

  if (isLoading) return <div className="card animate-pulse h-40" />
  if (isError || !quiz || quiz.questions.length === 0) return null

  const questions = quiz.questions
  const question = questions[current]
  const answeredCount = questions.filter(q => (answers[q.id] ?? []).length > 0).length
  const remaining = quiz.timeLimitSec - elapsed

  function start() {
    setAnswers({})
    setCurrent(0)
    setElapsed(0)
    setResult(null)
    startedAt.current = Date.now()
    setPhase('answering')
  }

  function send() {
    if (!quiz) return
    const payload: Record<number, string | string[]> = {}
    for (const q of questions) {
      const sel = answers[q.id] ?? []
      payload[q.id] = q.questionType === 'MULTIPLE_CHOICE' ? sel : (sel[0] ?? '')
    }
    submit.mutate({ quizId: quiz.id, answers: payload, timeTakenSec: Math.max(1, elapsed) })
  }

  return (
    <section className="card" aria-labelledby="module-quiz-title">
      <div className="flex items-start justify-between gap-4 mb-5">
        <div>
          <h2 id="module-quiz-title" className="text-lg font-semibold text-white flex items-center gap-2">
            <ClipboardCheck className="w-5 h-5 text-primary-400" />
            Evalúa lo que aprendiste
          </h2>
          <p className="text-sm text-slate-400 mt-1">{quiz.title}</p>
        </div>
        {quiz.passed && phase !== 'result' && (
          <span className="badge-safe flex-shrink-0">Aprobado · {quiz.bestScore}%</span>
        )}
      </div>

      {phase === 'intro' && (
        <div className="space-y-4">
          <div className="grid grid-cols-3 gap-3 text-center">
            <div className="rounded-lg bg-dark-800/60 p-3">
              <p className="text-xl font-bold text-white">{questions.length}</p>
              <p className="text-xs text-slate-500">preguntas</p>
            </div>
            <div className="rounded-lg bg-dark-800/60 p-3">
              <p className="text-xl font-bold text-white">{quiz.passingScore}%</p>
              <p className="text-xs text-slate-500">para aprobar</p>
            </div>
            <div className="rounded-lg bg-dark-800/60 p-3">
              <p className="text-xl font-bold text-white">{quiz.passed ? '✓' : `+${quiz.pointsReward}`}</p>
              <p className="text-xs text-slate-500">{quiz.passed ? 'puntos obtenidos' : 'puntos'}</p>
            </div>
          </div>
          {quiz.attempts > 0 && (
            <p className="text-sm text-slate-400">
              Llevas {quiz.attempts} {quiz.attempts === 1 ? 'intento' : 'intentos'}. Tu mejor puntaje: {quiz.bestScore}%.
            </p>
          )}
          <button type="button" onClick={start} className="btn-primary w-full">
            {quiz.attempts > 0 ? 'Volver a presentar el quiz' : 'Comenzar quiz'}
          </button>
        </div>
      )}

      {phase === 'answering' && question && (
        <div className="space-y-5">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Pregunta {current + 1} de {questions.length}</span>
            <span className={`flex items-center gap-1 ${remaining < 60 ? 'text-warning-400' : ''}`}>
              <Clock className="w-3.5 h-3.5" />
              {remaining > 0 ? formatTime(remaining) : 'Tiempo sugerido agotado'}
            </span>
          </div>
          <div className="h-1.5 rounded-full bg-slate-700 overflow-hidden">
            <div className="h-full bg-primary-500 transition-all" style={{ width: `${(answeredCount / questions.length) * 100}%` }} />
          </div>

          <QuestionCard
            question={question}
            selected={answers[question.id] ?? []}
            onChange={(value) => setAnswers(prev => ({ ...prev, [question.id]: value }))}
          />

          <div className="flex items-center justify-between gap-3">
            <button
              type="button"
              onClick={() => setCurrent(c => c - 1)}
              disabled={current === 0}
              className="btn-ghost inline-flex items-center gap-1 disabled:opacity-40"
            >
              <ChevronLeft className="w-4 h-4" /> Anterior
            </button>
            {current < questions.length - 1 ? (
              <button type="button" onClick={() => setCurrent(c => c + 1)} className="btn-primary inline-flex items-center gap-1">
                Siguiente <ChevronRight className="w-4 h-4" />
              </button>
            ) : (
              <button
                type="button"
                onClick={send}
                disabled={submit.isPending || answeredCount < questions.length}
                className="btn-primary disabled:opacity-50"
                title={answeredCount < questions.length ? 'Responde todas las preguntas' : undefined}
              >
                {submit.isPending ? 'Calificando...' : 'Enviar respuestas'}
              </button>
            )}
          </div>
          {current === questions.length - 1 && answeredCount < questions.length && (
            <p className="text-xs text-warning-400 text-right">
              Te faltan {questions.length - answeredCount} {questions.length - answeredCount === 1 ? 'pregunta' : 'preguntas'} por responder
            </p>
          )}
        </div>
      )}

      {phase === 'result' && result && (
        <ResultView result={result} questions={questions} onRetry={start} />
      )}
    </section>
  )
}
