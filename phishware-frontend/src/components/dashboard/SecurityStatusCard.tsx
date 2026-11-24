import { Link } from 'react-router-dom'
import { ArrowRight, Bell, ShieldAlert, ShieldCheck, Star } from 'lucide-react'
import { useLang } from '@/i18n/LanguageContext'

interface Props {
  username?: string
  threatsDetected: number
  unreadAlerts: number
  totalAnalyses: number
  safeUrls: number
  level?: number
  points?: number
}

/** Hero del dashboard. El estado se deriva SOLO de datos reales del backend. */
export default function SecurityStatusCard({
  username, threatsDetected, unreadAlerts, totalAnalyses, safeUrls, level, points,
}: Props) {
  const { t } = useLang()
  const calm = threatsDetected === 0 && unreadAlerts === 0
  // Porcentaje de enlaces seguros sobre el total analizado (100 si aún no hay análisis)
  const safePct = totalAnalyses > 0 ? Math.round((safeUrls / totalAnalyses) * 100) : 100
  const R = 52
  const C = 2 * Math.PI * R

  return (
    <section className="relative overflow-hidden rounded-[2rem] bg-gradient-to-br from-primary-800 via-primary-600 to-primary-500 p-6 sm:p-8 text-white shadow-[0_30px_60px_-25px_rgba(77,7,27,0.75)]">
      {/* Decoración */}
      <div className="pointer-events-none absolute -right-20 -top-24 h-72 w-72 rounded-full bg-white/10 blur-2xl animate-blob" aria-hidden />
      <div className="pointer-events-none absolute -bottom-24 left-1/3 h-56 w-56 rounded-full bg-primary-300/20 blur-3xl" aria-hidden />

      <div className="relative flex flex-col-reverse gap-6 md:flex-row md:items-center md:justify-between">
        <div className="max-w-xl">
          <p className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3 py-1 text-xs font-semibold uppercase tracking-wider backdrop-blur">
            <span className={`h-2 w-2 rounded-full ${calm ? 'bg-success-300' : 'bg-warning-300'} animate-pulse`} />
            {t.dashboard.secured}
          </p>
          <h1 className="mt-4 text-3xl font-bold leading-tight tracking-tight sm:text-4xl">
            {t.dashboard.hello}{username ? `, ${username}` : ''}.
          </h1>
          <p className="mt-2 flex items-center gap-2 text-lg font-semibold text-white/95">
            {calm ? <ShieldCheck className="h-5 w-5" /> : <ShieldAlert className="h-5 w-5" />}
            {calm ? t.dashboard.calm : t.dashboard.threatsUnread(threatsDetected, unreadAlerts)}
          </p>
          <p className="mt-1 text-sm text-white/75">
            {totalAnalyses === 0 ? t.dashboard.empty : t.dashboard.totalAnalyses(totalAnalyses)}
          </p>

          <div className="mt-6 flex flex-wrap items-center gap-2">
            <Link
              to="/analysis"
              className="inline-flex items-center gap-2 rounded-2xl bg-white px-5 py-2.5 text-sm font-semibold text-primary-700 shadow-lg transition-all hover:-translate-y-0.5 hover:shadow-xl"
            >
              {t.dashboard.qa1t} <ArrowRight className="h-4 w-4" />
            </Link>
            {unreadAlerts > 0 && (
              <Link
                to="/alerts"
                className="inline-flex items-center gap-2 rounded-2xl border border-white/30 bg-white/10 px-5 py-2.5 text-sm font-semibold backdrop-blur transition-all hover:bg-white/20"
              >
                <Bell className="h-4 w-4" /> {t.alerts.title} ({unreadAlerts})
              </Link>
            )}
            {level !== undefined && (
              <span className="inline-flex items-center gap-1.5 rounded-2xl border border-white/20 bg-white/10 px-3 py-2 text-xs font-semibold tabular-nums backdrop-blur">
                <Star className="h-3.5 w-3.5" /> {t.common.level} {level} · {(points ?? 0).toLocaleString()} {t.common.points}
              </span>
            )}
          </div>
        </div>

        {/* Anillo de protección */}
        <div className="relative mx-auto h-36 w-36 shrink-0 md:mx-0 md:h-44 md:w-44 animate-float">
          <svg viewBox="0 0 120 120" className="h-full w-full -rotate-90" role="img" aria-label={`${safePct}%`}>
            <circle cx="60" cy="60" r={R} fill="none" stroke="rgba(255,255,255,0.18)" strokeWidth="10" />
            <circle
              cx="60" cy="60" r={R} fill="none" stroke="white" strokeWidth="10" strokeLinecap="round"
              strokeDasharray={C} strokeDashoffset={C * (1 - safePct / 100)}
              style={{ transition: 'stroke-dashoffset 1.2s cubic-bezier(0.16,1,0.3,1)' }}
            />
          </svg>
          <div className="absolute inset-0 grid place-items-center text-center">
            <div>
              <ShieldCheck className="mx-auto h-7 w-7" />
              <p className="mt-1 text-2xl font-bold tabular-nums">{safePct}%</p>
              <p className="text-[11px] font-medium text-white/75">{t.dashboard.stats.safe}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
