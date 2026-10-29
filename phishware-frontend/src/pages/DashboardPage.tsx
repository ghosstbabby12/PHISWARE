import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  Activity, BellRing, History, QrCode, Search, ShieldCheck, Star, CheckCircle, XCircle,
} from 'lucide-react'
import { dashboardService } from '@/services/dashboardService'
import StatCard from '@/components/dashboard/StatCard'
import SecurityStatusCard from '@/components/dashboard/SecurityStatusCard'
import QuickActionCard from '@/components/dashboard/QuickActionCard'
import RecentAnalysisList from '@/components/dashboard/RecentAnalysisList'
import AppHeader from '@/components/common/AppHeader'
import { ErrorState, LoadingState } from '@/components/common/FeedbackStates'
import { useAuth } from '@/context/AuthContext'
import { useLang } from '@/i18n/LanguageContext'

export default function DashboardPage() {
  const { user } = useAuth()
  const { t } = useLang()

  const { data: dashboard, isLoading, isError, refetch, isFetching } = useQuery({
    queryKey: ['dashboard'],
    queryFn: dashboardService.getDashboard,
    refetchInterval: 60000,
    retry: 1,
  })

  if (isLoading) {
    return (
      <div className="space-y-6 animate-fade-in">
        <AppHeader title={t.dashboard.title} subtitle={`${t.dashboard.helloBack}, ${user?.username ?? ''}.`} />
        <LoadingState message={`${t.common.loading}`} rows={4} />
      </div>
    )
  }

  if (isError || !dashboard) {
    return (
      <div className="space-y-6 animate-fade-in">
        <AppHeader title={t.dashboard.title} subtitle={t.dashboard.summary} />
        <ErrorState
          title={t.dashboard.loadFail}
          description={t.dashboard.loadFailSub}
          onRetry={() => refetch()}
        />
      </div>
    )
  }

  const nextLevelPoints = 100 * Math.pow(2, user?.level ?? 1)
  const progressPct = Math.min(((user?.points ?? 0) / nextLevelPoints) * 100, 100)

  return (
    <div className="space-y-6 animate-fade-in">
      <AppHeader
        title={t.dashboard.title}
        subtitle={`${t.dashboard.hello}, ${user?.username}. ${t.dashboard.subtitleUser}`}
        actions={
          <span className="badge-primary">
            <Star className="w-3 h-3" /> {t.common.level} {user?.level} · {(user?.points ?? 0).toLocaleString()} {t.common.points}
          </span>
        }
      />

      <SecurityStatusCard
        threatsDetected={dashboard.threatsDetected}
        unreadAlerts={dashboard.unreadAlerts}
        totalAnalyses={dashboard.totalAnalyses}
      />

      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4" aria-label={t.dashboard.summary}>
        <StatCard title={t.dashboard.stats.total} value={dashboard.totalAnalyses} icon={Activity} subtitle={t.dashboard.stats.accumulated} />
        <StatCard title={t.dashboard.stats.threats} value={dashboard.threatsDetected} icon={ShieldCheck} iconClass="text-danger-500" subtitle={t.dashboard.stats.requireAttention} />
        <StatCard title={t.dashboard.stats.safe} value={dashboard.safeUrls} icon={CheckCircle} iconClass="text-success-500" subtitle={t.dashboard.stats.noRisk} />
        <StatCard title={t.dashboard.stats.dangerous} value={dashboard.dangerousUrls} icon={XCircle} iconClass="text-danger-500" subtitle={`+ ${dashboard.suspiciousUrls} ${t.dashboard.stats.suspicious}`} />
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4" aria-label={t.dashboard.quick}>
        <QuickActionCard to="/analysis" icon={Search} title={t.dashboard.qa1t} description={t.dashboard.qa1d} />
        <QuickActionCard to="/analysis" icon={QrCode} title={t.dashboard.qa2t} description={t.dashboard.qa2d} />
        <QuickActionCard to="/alerts" icon={BellRing} title={t.dashboard.qa3t} description={dashboard.unreadAlerts > 0 ? t.dashboard.qa3dUnread(dashboard.unreadAlerts) : t.dashboard.qa3d} />
        <QuickActionCard to="/history" icon={History} title={t.dashboard.qa4t} description={t.dashboard.qa4d} />
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-5">
        <div className="xl:col-span-2">
          <RecentAnalysisList items={dashboard.recentAnalyses ?? []} />
          {isFetching && <p className="text-xs text-ink-500 mt-2" role="status">{t.common.loading}</p>}
        </div>
        <div className="card flex flex-col justify-between">
          <div>
            <h2 className="font-semibold text-ink-900">{t.dashboard.nextLevel}</h2>
            <p className="text-4xl font-bold text-primary-700 mt-3 tabular-nums">{t.common.level} {user?.level}</p>
            <p className="text-ink-500 text-sm mt-1">{t.dashboard.levelOf(user?.points ?? 0)}</p>
          </div>
          <div className="mt-5">
            <div className="flex justify-between text-xs text-ink-500 mb-2">
              <span>{t.dashboard.nextLevel}</span>
              <span className="tabular-nums">{progressPct.toFixed(0)}%</span>
            </div>
            <div className="h-2.5 bg-ink-100 rounded-full overflow-hidden" role="progressbar" aria-valuenow={Math.round(progressPct)} aria-valuemin={0} aria-valuemax={100} aria-label={t.dashboard.nextLevel}>
              <div className="h-full bg-primary-600 rounded-full transition-all duration-700" style={{ width: `${progressPct}%` }} />
            </div>
            <Link to="/education" className="btn-secondary w-full mt-4 !py-2 text-sm">
              {t.dashboard.keepLearning}
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
