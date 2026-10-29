import { useAuth } from '@/context/AuthContext'
import { useLang } from '@/i18n/LanguageContext'
import { User, Mail, Star, Shield, Activity } from 'lucide-react'
import AppHeader from '@/components/common/AppHeader'

export default function ProfilePage() {
  const { user, isAdmin } = useAuth()
  const { t, lang } = useLang()
  const es = lang === 'es'

  const levelThresholds = [0, 100, 250, 500, 800, 1200, 1700, 2300, 3000, 4000]
  const currentThreshold = levelThresholds[(user?.level ?? 1) - 1] ?? 0
  const nextThreshold = levelThresholds[user?.level ?? 1] ?? ((user?.points ?? 0) + 1)
  const denominator = nextThreshold - currentThreshold
  const progress = denominator > 0 ? (((user?.points ?? 0) - currentThreshold) / denominator) * 100 : 100

  return (
    <div className="max-w-2xl mx-auto space-y-6 animate-fade-in">
      <AppHeader title={t.nav.profile} subtitle={`@${user?.username}`} />

      <div className="card">
        <div className="flex items-center gap-6">
          <div className="w-20 h-20 rounded-full bg-primary-600 flex items-center justify-center text-3xl font-bold text-white shrink-0" aria-hidden>
            {user?.username?.[0]?.toUpperCase()}
          </div>
          <div>
            <h2 className="text-xl font-bold text-ink-900">{user?.fullName || user?.username}</h2>
            <p className="text-ink-500 text-sm">@{user?.username}</p>
            <div className="flex flex-wrap gap-2 mt-2">
              {user?.roles.map(role => (
                <span key={role} className="text-xs px-2 py-0.5 rounded-full bg-primary-100 text-primary-700 border border-primary-600/15 font-semibold">
                  {role.replace('ROLE_', '')}
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="card space-y-4">
        <h3 className="font-semibold text-ink-900">{t.settings.account}</h3>
        <div className="flex items-center gap-3 text-ink-700">
          <User className="w-4 h-4 text-ink-300" aria-hidden />
          <span className="text-sm">{user?.username}</span>
        </div>
        <div className="flex items-center gap-3 text-ink-700">
          <Mail className="w-4 h-4 text-ink-300" aria-hidden />
          <span className="text-sm">{user?.email}</span>
        </div>
        {isAdmin && (
          <div className="flex items-center gap-2 text-sm font-semibold text-warning-600">
            <Shield className="w-4 h-4" aria-hidden />
            {t.nav.admin}
          </div>
        )}
      </div>

      <div className="card space-y-4">
        <h3 className="font-semibold text-ink-900 flex items-center gap-2">
          <Star className="w-5 h-5 text-warning-500" aria-hidden />
          {es ? 'Progreso y gamificación' : 'Progress and gamification'}
        </h3>

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-ink-50 border divider rounded-xl p-4 text-center">
            <p className="text-3xl font-bold text-primary-700 tabular-nums">{user?.level}</p>
            <p className="text-sm text-ink-500 mt-1">{es ? 'Nivel actual' : 'Current level'}</p>
          </div>
          <div className="bg-ink-50 border divider rounded-xl p-4 text-center">
            <p className="text-3xl font-bold text-warning-600 tabular-nums">{user?.points?.toLocaleString('es-CO')}</p>
            <p className="text-sm text-ink-500 mt-1">{es ? 'Puntos totales' : 'Total points'}</p>
          </div>
        </div>

        <div>
          <div className="flex justify-between text-sm text-ink-500 mb-2">
            <span>{es ? `Progreso al nivel ${(user?.level ?? 1) + 1}` : `Progress to level ${(user?.level ?? 1) + 1}`}</span>
            <span className="tabular-nums">{Math.min(Math.round(progress), 100)}%</span>
          </div>
          <div className="h-3 bg-ink-100 rounded-full overflow-hidden" role="progressbar" aria-valuenow={Math.round(Math.min(progress, 100))} aria-valuemin={0} aria-valuemax={100} aria-label={t.dashboard.nextLevel}>
            <div
              className="h-full bg-gradient-to-r from-primary-700 to-primary-500 rounded-full transition-all duration-700"
              style={{ width: `${Math.min(progress, 100)}%` }}
            />
          </div>
          <p className="text-xs text-ink-500 mt-1.5 tabular-nums">
            {nextThreshold - (user?.points ?? 0)} {es ? 'puntos para el siguiente nivel' : 'points to the next level'}
          </p>
        </div>
      </div>

      <div className="card !bg-primary-50/60 !border-primary-600/15">
        <h3 className="font-semibold text-ink-900 flex items-center gap-2 mb-3">
          <Activity className="w-4 h-4 text-primary-700" aria-hidden />
          {es ? 'Cómo ganar puntos' : 'How to earn points'}
        </h3>
        <ul className="space-y-2 text-sm text-ink-700">
          <li className="flex justify-between"><span>{es ? 'Analizar una URL' : 'Analyze a URL'}</span><span className="text-primary-700 font-mono font-semibold">+5 pts</span></li>
          <li className="flex justify-between"><span>{es ? 'Detectar una amenaza real' : 'Detect a real threat'}</span><span className="text-primary-700 font-mono font-semibold">+15 pts</span></li>
          <li className="flex justify-between"><span>{es ? 'Completar un quiz' : 'Complete a quiz'}</span><span className="text-primary-700 font-mono font-semibold">+50 pts</span></li>
          <li className="flex justify-between"><span>{es ? 'Puntuación perfecta en quiz' : 'Perfect quiz score'}</span><span className="text-primary-700 font-mono font-semibold">+100 pts</span></li>
        </ul>
      </div>
    </div>
  )
}
