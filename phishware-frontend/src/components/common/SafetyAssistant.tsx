import { useEffect, useMemo, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { ChevronLeft, ChevronRight, Flame, Lightbulb, Sparkles, Target, X, Check, XCircle } from 'lucide-react'
import clsx from 'clsx'
import { useLang } from '@/i18n/LanguageContext'

type Tab = 'tips' | 'game'

/**
 * Burbuja flotante (abajo a la izquierda) con consejos según la página
 * y un reto rápido "¿legítimo o phishing?". Todo es local: no llama al backend.
 */
export default function SafetyAssistant() {
  const { t } = useLang()
  const { pathname } = useLocation()
  const [open, setOpen] = useState(false)
  const [tab, setTab] = useState<Tab>('tips')
  const [tipIdx, setTipIdx] = useState(0)

  const tips = useMemo(() => {
    if (pathname.startsWith('/analysis')) return t.fx.tips.analysis
    if (pathname.startsWith('/education')) return t.fx.tips.education
    return t.fx.tips.default
  }, [pathname, t])

  useEffect(() => setTipIdx(0), [tips])

  // Juego
  const challenges = t.fx.challenges
  const [order, setOrder] = useState<number[]>(() => shuffle(challenges.length))
  const [pos, setPos] = useState(0)
  const [answer, setAnswer] = useState<boolean | null>(null)
  const [streak, setStreak] = useState(0)
  const current = challenges[order[pos % order.length]]
  const correct = answer !== null && answer === current.phishing

  function answerWith(isPhishing: boolean) {
    if (answer !== null) return
    setAnswer(isPhishing)
    setStreak(s => (isPhishing === current.phishing ? s + 1 : 0))
  }

  function nextChallenge() {
    setAnswer(null)
    if (pos + 1 >= order.length) {
      setOrder(shuffle(challenges.length))
      setPos(0)
    } else {
      setPos(p => p + 1)
    }
  }

  return (
    <div className="fixed left-4 bottom-28 lg:left-[1.375rem] lg:bottom-6 z-40 flex flex-col items-start gap-3">
      {open && (
        <div
          role="dialog"
          aria-label={t.fx.assistant}
          className="glass-strong w-[min(22rem,calc(100vw-2rem))] rounded-[1.75rem] p-4 animate-scale-in origin-bottom-left"
        >
          <div className="flex items-center justify-between">
            <p className="flex items-center gap-2 text-sm font-bold text-ink-900 dark:text-ink-50">
              <span className="grid h-7 w-7 place-items-center rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 text-white">
                <Sparkles className="h-4 w-4" />
              </span>
              {t.fx.assistant}
            </p>
            <button type="button" onClick={() => setOpen(false)} className="text-ink-500 hover:text-ink-900 dark:hover:text-white" aria-label={t.common.close}>
              <X className="h-4 w-4" />
            </button>
          </div>

          {/* Pestañas segmentadas */}
          <div className="relative mt-3 grid grid-cols-2 rounded-2xl bg-ink-100/80 p-1 text-xs font-semibold dark:bg-white/5" role="tablist">
            <span
              className={clsx('absolute inset-y-1 w-[calc(50%-0.25rem)] rounded-xl bg-white shadow-sm transition-transform duration-300 dark:bg-white/15',
                tab === 'game' ? 'translate-x-full' : 'translate-x-0')}
              style={{ left: '0.25rem' }}
              aria-hidden
            />
            {([['tips', Lightbulb, t.fx.tipsTab], ['game', Target, t.fx.gameTab]] as const).map(([id, Icon, label]) => (
              <button
                key={id}
                type="button"
                role="tab"
                aria-selected={tab === id}
                onClick={() => setTab(id)}
                className={clsx('relative z-10 flex items-center justify-center gap-1.5 rounded-xl py-1.5 transition-colors',
                  tab === id ? 'text-primary-700 dark:text-white' : 'text-ink-500 dark:text-ink-300')}
              >
                <Icon className="h-3.5 w-3.5" /> {label}
              </button>
            ))}
          </div>

          {tab === 'tips' ? (
            <div className="mt-4">
              <p key={tipIdx} className="min-h-[4.5rem] text-sm leading-relaxed text-ink-700 animate-fade-in dark:text-ink-200">
                {tips[tipIdx]}
              </p>
              <div className="mt-3 flex items-center justify-between">
                <div className="flex gap-1.5" aria-hidden>
                  {tips.map((_, i) => (
                    <span key={i} className={clsx('h-1.5 rounded-full transition-all duration-300',
                      i === tipIdx ? 'w-5 bg-primary-600' : 'w-1.5 bg-ink-300 dark:bg-white/20')} />
                  ))}
                </div>
                <div className="flex items-center gap-1">
                  <span className="mr-1 text-[11px] tabular-nums text-ink-500 dark:text-ink-300">{t.fx.tipOf(tipIdx + 1, tips.length)}</span>
                  <button type="button" className="btn-ghost !p-1.5" aria-label={t.fx.prev}
                    onClick={() => setTipIdx(i => (i - 1 + tips.length) % tips.length)}>
                    <ChevronLeft className="h-4 w-4" />
                  </button>
                  <button type="button" className="btn-ghost !p-1.5" aria-label={t.fx.next}
                    onClick={() => setTipIdx(i => (i + 1) % tips.length)}>
                    <ChevronRight className="h-4 w-4" />
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="mt-4 space-y-3">
              <div className="flex items-center justify-between">
                <p className="text-xs font-semibold text-ink-500 dark:text-ink-300">{t.fx.gameIntro}</p>
                <span className="inline-flex items-center gap-1 rounded-full bg-warning-500/10 px-2 py-0.5 text-[11px] font-bold text-warning-600 tabular-nums">
                  <Flame className="h-3 w-3" /> {t.fx.streak(streak)}
                </span>
              </div>
              <p className="break-all rounded-2xl border border-ink-200/80 bg-white/70 px-3 py-2.5 font-mono text-[12px] text-ink-900 dark:border-white/10 dark:bg-white/5 dark:text-ink-50">
                {current.url}
              </p>
              {answer === null ? (
                <div className="grid grid-cols-2 gap-2">
                  <button type="button" onClick={() => answerWith(false)}
                    className="rounded-2xl border border-success-500/30 bg-success-500/10 py-2 text-sm font-semibold text-success-600 transition-all hover:-translate-y-0.5 hover:bg-success-500/15">
                    {t.fx.legit}
                  </button>
                  <button type="button" onClick={() => answerWith(true)}
                    className="rounded-2xl border border-danger-500/30 bg-danger-500/10 py-2 text-sm font-semibold text-danger-600 transition-all hover:-translate-y-0.5 hover:bg-danger-500/15">
                    {t.fx.phishing}
                  </button>
                </div>
              ) : (
                <div className={clsx('rounded-2xl border p-3 animate-scale-in',
                  correct ? 'border-success-500/30 bg-success-500/10' : 'border-danger-500/30 bg-danger-500/10')} role="status">
                  <p className={clsx('flex items-center gap-1.5 text-sm font-bold', correct ? 'text-success-600' : 'text-danger-600')}>
                    {correct ? <Check className="h-4 w-4" /> : <XCircle className="h-4 w-4" />}
                    {correct ? t.fx.correct : t.fx.wrong} · {current.phishing ? t.fx.phishing : t.fx.legit}
                  </p>
                  <p className="mt-1 text-xs leading-relaxed text-ink-700 dark:text-ink-200">{current.why}</p>
                  <button type="button" onClick={nextChallenge} className="btn-primary mt-3 w-full !py-2 text-xs">
                    {t.fx.nextChallenge} <ChevronRight className="h-3.5 w-3.5" />
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      <button
        type="button"
        onClick={() => setOpen(v => !v)}
        aria-expanded={open}
        aria-label={t.fx.openAssistant}
        className={clsx('group grid h-12 w-12 place-items-center rounded-full glass-strong text-primary-700 transition-all duration-300 hover:scale-105 dark:text-primary-300',
          !open && 'animate-float')}
      >
        {open ? <X className="h-5 w-5" /> : <Sparkles className="h-5 w-5 transition-transform group-hover:rotate-12" />}
      </button>
    </div>
  )
}

function shuffle(n: number): number[] {
  const a = Array.from({ length: n }, (_, i) => i)
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]]
  }
  return a
}
