import { useAuth } from '@/context/AuthContext'
import { useLang } from '@/i18n/LanguageContext'
import { Award, Flame, Mail, Shield, Star, User, Activity } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import AppHeader from '@/components/common/AppHeader'
import { gamificationService } from '@/services/gamificationService'

export default function ProfilePage() {
  const { user, isAdmin } = useAuth()
  const { t } = useLang()

  const { data: badges = [] } = useQuery({
    queryKey: ['badges'],
    queryFn: gamificationService.getBadges,
  })
  const { data: streak } = useQuery({
    queryKey: ['streak'],
    queryFn: gamificationService.getStreak,
  })

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
            <h2 className="text-xl font-bold text-ink-900 dark:text-ink-50">{user?.fullName || user?.username}</h2>
            <p className="text-ink-500 dark:text-ink-300 text-sm">@{user?.username}</p>
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
        <h3 className="font-semibold text-ink-900 dark:text-ink-50">{t.settings.account}</h3>
        <div className="flex items-center gap-3 text-ink-700 dark:text-ink-200">
          <User className="w-4 h-4 text-ink-300 dark:text-ink-500" aria-hidden />
          <span className="text-sm">{user?.username}</span>
        </div>
        <div className="flex items-center gap-3 text-ink-700 dark:text-ink-200">
          <Mail className="w-4 h-4 text-ink-300 dark:text-ink-500" aria-hidden />
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
        <h3 className="font-semibold text-ink-900 dark:text-ink-50 flex items-center gap-2">
          <Star className="w-5 h-5 text-warning-500" aria-hidden />
          {t.profile.progress}
        </h3>

        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          <div className="bg-ink-50 dark:bg-dark-900 border divider rounded-xl p-4 text-center">
            <p className="text-3xl font-bold text-primary-700 tabular-nums">{user?.level}</p>
            <p className="text-sm text-ink-500 dark:text-ink-300 mt-1">{t.profile.level}</p>
          </div>
          <div className="bg-ink-50 dark:bg-dark-900 border divider rounded-xl p-4 text-center">
            <p className="text-3xl font-bold text-warning-600 tabular-nums">{user?.points?.toLocaleString('es-CO')}</p>
            <p className="text-sm text-ink-500 dark:text-ink-300 mt-1">{t.profile.points}</p>
          </div>
          <div className="bg-ink-50 dark:bg-dark-900 border divider rounded-xl p-4 text-center col-span-2 sm:col-span-1">
            <p className="text-3xl font-bold text-danger-500 tabular-nums flex items-center justify-center gap-1">
              <Flame className="w-6 h-6" aria-hidden />
              {streak?.currentStreakDays ?? 0}
            </p>
            <p className="text-sm text-ink-500 dark:text-ink-300 mt-1">{t.profile.streak}</p>
          </div>
        </div>

        <p className="text-xs text-ink-500 dark:text-ink-300">
          {(streak?.currentStreakDays ?? 0) > 0
            ? t.profile.streakUnit(streak?.currentStreakDays ?? 0)
            : t.profile.streakNone}
        </p>

        <div>
          <div className="flex justify-between text-sm text-ink-500 dark:text-ink-300 mb-2">
            <span>{t.profile.progressTo((user?.level ?? 1) + 1)}</span>
            <span className="tabular-nums">{Math.min(Math.round(progress), 100)}%</span>
          </div>
          <div className="h-3 bg-ink-100 dark:bg-dark-800 rounded-full overflow-hidden" role="progressbar" aria-valuenow={Math.round(Math.min(progress, 100))} aria-valuemin={0} aria-valuemax={100} aria-label={t.dashboard.nextLevel}>
            <div
              className="h-full bg-gradient-to-r from-primary-700 to-primary-500 rounded-full transition-all duration-700"
              style={{ width: `${Math.min(progress, 100)}%` }}
            />
          </div>
          <p className="text-xs text-ink-500 dark:text-ink-300 mt-1.5 tabular-nums">
            {t.profile.pointsToNext(nextThreshold - (user?.points ?? 0))}
          </p>
        </div>
      </div>

      <div className="card !bg-primary-50/60 !border-primary-600/15">
        <h3 className="font-semibold text-ink-900 dark:text-ink-50 flex items-center gap-2 mb-3">
          <Award className="w-4 h-4 text-primary-700" aria-hidden />
          {t.profile.badges}
        </h3>
        <p className="text-xs text-ink-500 dark:text-ink-300 mb-3">{t.profile.badgesSub}</p>
        <ul className="grid grid-cols-2 sm:grid-cols-3 gap-2">
          {badges.map(b => (
            <li
              key={b.id}
              title={b.description}
              className={`rounded-xl border px-3 py-2.5 text-center transition-colors ${b.earned
                ? 'border-warning-500/40 bg-warning-500/10'
                : 'border divider bg-ink-50 dark:bg-dark-900 opacity-70'}`}
            >
              <Award className={`w-5 h-5 mx-auto ${b.earned ? 'text-warning-500' : 'text-ink-300 dark:text-ink-500'}`} aria-hidden />
              <p className="text-xs font-semibold text-ink-900 dark:text-ink-50 mt-1 line-clamp-1">{b.name}</p>
              <p className="text-[11px] text-ink-500 dark:text-ink-300">+{b.pointsReward} pts</p>
              {!b.earned && <p className="text-[11px] text-ink-500 dark:text-ink-300">{t.profile.locked}</p>}
            </li>
          ))}
        </ul>
      </div>

      <div className="card !bg-primary-50/60 !border-primary-600/15">
        <h3 className="font-semibold text-ink-900 dark:text-ink-50 flex items-center gap-2 mb-3">
          <Activity className="w-4 h-4 text-primary-700" aria-hidden />
          {t.profile.howToEarn}
        </h3>
        <ul className="space-y-2 text-sm text-ink-700 dark:text-ink-200">
          <li className="flex justify-between"><span>{t.profile.earn1}</span><span className="text-primary-700 font-mono font-semibold">+5 pts</span></li>
          <li className="flex justify-between"><span>{t.profile.earn2}</span><span className="text-primary-700 font-mono font-semibold">+15 pts</span></li>
          <li className="flex justify-between"><span>{t.profile.earn3}</span><span className="text-primary-700 font-mono font-semibold">+50 pts</span></li>
          <li className="flex justify-between"><span>{t.profile.earn4}</span><span className="text-primary-700 font-mono font-semibold">+100 pts</span></li>
        </ul>
      </div>
    </div>
  )
}
