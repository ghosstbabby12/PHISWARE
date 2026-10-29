import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bell, BellOff, CheckCheck } from 'lucide-react'
import { dashboardService } from '@/services/dashboardService'
import { useLang } from '@/i18n/LanguageContext'
import toast from 'react-hot-toast'
import clsx from 'clsx'
import AppHeader from '@/components/common/AppHeader'
import ConfirmationDialog from '@/components/common/ConfirmationDialog'
import { EmptyState, ErrorState, LoadingState } from '@/components/common/FeedbackStates'

const SEVERITY_STYLES = {
  LOW:      { bar: 'border-l-success-500', chip: 'bg-success-500/10 text-success-600 border-success-500/25' },
  MEDIUM:   { bar: 'border-l-warning-500', chip: 'bg-warning-500/10 text-warning-600 border-warning-500/25' },
  HIGH:     { bar: 'border-l-danger-500', chip: 'bg-danger-500/10 text-danger-600 border-danger-500/25' },
  CRITICAL: { bar: 'border-l-danger-600', chip: 'bg-danger-600/10 text-danger-700 border-danger-600/25' },
} as const

function formatDate(iso: string, locale: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString(locale, { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })
}

export default function AlertsPage() {
  const { t, lang } = useLang()
  const queryClient = useQueryClient()
  const [confirmAll, setConfirmAll] = useState(false)

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['alerts'],
    queryFn: () => dashboardService.getAlerts(),
  })

  const markAllRead = useMutation({
    mutationFn: dashboardService.markAllAlertsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alerts'] })
      queryClient.invalidateQueries({ queryKey: ['unread-alerts'] })
      setConfirmAll(false)
      toast.success(t.alerts.marked)
    },
    onError: () => toast.error(t.alerts.markFail),
  })

  const markRead = useMutation({
    mutationFn: dashboardService.markAlertRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alerts'] })
      queryClient.invalidateQueries({ queryKey: ['unread-alerts'] })
    },
    onError: () => toast.error(t.alerts.markOneFail),
  })

  const unreadCount = data?.content.filter(a => !a.isRead).length ?? 0

  return (
    <div className="space-y-6 animate-fade-in">
      <AppHeader
        title={t.alerts.title}
        subtitle={unreadCount > 0 ? t.alerts.unread(unreadCount, data?.content.length ?? 0) : t.alerts.allOk}
        actions={
          unreadCount > 0 ? (
            <button onClick={() => setConfirmAll(true)} className="btn-secondary text-sm">
              <CheckCheck className="w-4 h-4" /> {t.alerts.markAll}
            </button>
          ) : undefined
        }
      />

      {isLoading ? (
        <LoadingState message={t.alerts.load} rows={3} />
      ) : isError ? (
        <ErrorState
          title={t.alerts.loadFail}
          description={t.alerts.loadFailSub}
          onRetry={() => refetch()}
        />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          icon={BellOff}
          title={t.alerts.empty}
          description={t.alerts.emptySub}
        />
      ) : (
        <ul className="space-y-3" aria-label="Lista de alertas">
          {data.content.map((alert) => {
            const style = SEVERITY_STYLES[alert.severity as keyof typeof SEVERITY_STYLES] ?? SEVERITY_STYLES.LOW
            return (
              <li key={alert.id}>
                <button
                  type="button"
                  onClick={() => !alert.isRead && markRead.mutate(alert.id)}
                  disabled={alert.isRead || markRead.isPending}
                  className={clsx(
                    'card border-l-4 w-full text-left transition-all duration-200',
                    style.bar,
                    !alert.isRead ? 'hover:border-primary-200 hover:shadow-md cursor-pointer' : 'cursor-default',
                  )}
                  aria-label={alert.isRead ? `${alert.title} (${t.alerts.read})` : t.alerts.unreadAria(alert.title)}
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex flex-wrap items-center gap-2 mb-1">
                        <span className="flex items-center gap-1.5">
                          {!alert.isRead && <span className="w-2 h-2 rounded-full bg-primary-600 shrink-0" aria-hidden />}
                          <span className="font-semibold text-ink-900">{alert.title}</span>
                        </span>
                        <span className={clsx('inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold border', style.chip)}>
                          {alert.severity}
                        </span>
                      </div>
                      <p className="text-sm text-ink-700">{alert.message}</p>
                      {alert.recommendations?.length > 0 && (
                        <ul className="mt-2 space-y-1">
                          {alert.recommendations.map((rec, i) => (
                            <li key={i} className="text-xs text-ink-500 flex items-start gap-1.5">
                              <span className="mt-[5px] w-1 h-1 rounded-full bg-ink-300 shrink-0" aria-hidden />
                              {rec}
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                    <time dateTime={alert.createdAt} className="text-xs text-ink-500 whitespace-nowrap shrink-0">
                      {formatDate(alert.createdAt, lang === 'es' ? 'es-CO' : 'en-US')}
                    </time>
                  </div>
                </button>
              </li>
            )
          })}
        </ul>
      )}

      <ConfirmationDialog
        open={confirmAll}
        title="¿Marcar todas como leídas?"
        description="Esta acción actualiza el estado de tus alertas. Podrás seguir consultándolas en esta lista."
        confirmLabel={markAllRead.isPending ? 'Actualizando…' : 'Marcar todas'}
        onConfirm={() => markAllRead.mutate()}
        onCancel={() => setConfirmAll(false)}
      />

      <p className="text-xs text-ink-500 flex items-center gap-1.5">
        <Bell className="w-3.5 h-3.5" /> {t.alerts.listNote}
      </p>
    </div>
  )
}
