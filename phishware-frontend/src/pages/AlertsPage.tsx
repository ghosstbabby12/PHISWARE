import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bell, BellOff, CheckCheck } from 'lucide-react'
import { dashboardService } from '@/services/dashboardService'
import toast from 'react-hot-toast'
import clsx from 'clsx'

const SEVERITY_COLORS = {
  LOW:      'border-l-success-500 bg-success-500/5',
  MEDIUM:   'border-l-warning-500 bg-warning-500/5',
  HIGH:     'border-l-danger-500 bg-danger-500/5',
  CRITICAL: 'border-l-danger-600 bg-danger-600/10',
}

export default function AlertsPage() {
  const queryClient = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['alerts'],
    queryFn: () => dashboardService.getAlerts(),
  })

  const markAllRead = useMutation({
    mutationFn: dashboardService.markAllAlertsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alerts'] })
      queryClient.invalidateQueries({ queryKey: ['unread-alerts'] })
      toast.success('Todas las alertas marcadas como leídas')
    },
  })

  const markRead = useMutation({
    mutationFn: dashboardService.markAlertRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['alerts'] })
      queryClient.invalidateQueries({ queryKey: ['unread-alerts'] })
    },
  })

  const unreadCount = data?.content.filter(a => !a.isRead).length ?? 0

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <Bell className="w-6 h-6 text-primary-400" />
            Alertas de Seguridad
          </h1>
          <p className="text-slate-400 mt-1">
            {unreadCount > 0 ? `${unreadCount} alertas sin leer` : 'Todo al día'}
          </p>
        </div>
        {unreadCount > 0 && (
          <button
            onClick={() => markAllRead.mutate()}
            disabled={markAllRead.isPending}
            className="btn-ghost flex items-center gap-2"
          >
            <CheckCheck className="w-4 h-4" />
            Marcar todas como leídas
          </button>
        )}
      </div>

      {isLoading ? (
        <div className="flex justify-center py-20">
          <div className="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full" />
        </div>
      ) : data?.content.length === 0 ? (
        <div className="card text-center py-16">
          <BellOff className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <p className="text-slate-400">Sin alertas registradas</p>
        </div>
      ) : (
        <div className="space-y-3">
          {data?.content.map((alert) => (
            <div
              key={alert.id}
              onClick={() => !alert.isRead && markRead.mutate(alert.id)}
              className={clsx(
                'card border-l-4 cursor-pointer transition-all duration-200',
                SEVERITY_COLORS[alert.severity as keyof typeof SEVERITY_COLORS] ?? 'border-l-slate-500',
                !alert.isRead && 'hover:bg-slate-700/20'
              )}
            >
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <h3 className="font-semibold text-white">{alert.title}</h3>
                    {!alert.isRead && (
                      <span className="w-2 h-2 rounded-full bg-primary-500 flex-shrink-0" />
                    )}
                  </div>
                  <p className="text-sm text-slate-300">{alert.message}</p>
                  {alert.recommendations?.length > 0 && (
                    <ul className="mt-2 space-y-1">
                      {alert.recommendations.map((rec, i) => (
                        <li key={i} className="text-xs text-slate-400 flex items-start gap-1.5">
                          <span className="mt-1.5 w-1 h-1 rounded-full bg-slate-500 flex-shrink-0" />
                          {rec}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
                <p className="text-xs text-slate-500 whitespace-nowrap flex-shrink-0">
                  {new Date(alert.createdAt).toLocaleDateString('es-ES')}
                </p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
