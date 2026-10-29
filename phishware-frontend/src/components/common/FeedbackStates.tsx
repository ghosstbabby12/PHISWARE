import { AlertTriangle, Inbox, RefreshCw } from 'lucide-react'
import clsx from 'clsx'

export function LoadingState({ message = 'Cargando…', rows = 3 }: { message?: string; rows?: number }) {
  return (
    <div role="status" aria-live="polite" aria-label={message} className="card">
      <div className="flex items-center gap-3 mb-4">
        <div className="animate-spin w-5 h-5 border-2 border-primary-600 border-t-transparent rounded-full" aria-hidden />
        <p className="text-sm font-medium text-ink-700">{message}</p>
      </div>
      <div className="space-y-2.5" aria-hidden>
        {Array.from({ length: rows }).map((_, i) => (
          <div key={i} className="skeleton h-11" style={{ opacity: 1 - i * 0.22 }} />
        ))}
      </div>
    </div>
  )
}

export function EmptyState({
  icon: Icon = Inbox,
  title,
  description,
  action,
}: {
  icon?: React.ComponentType<{ className?: string }>
  title: string
  description?: string
  action?: React.ReactNode
}) {
  return (
    <div className="card text-center py-14">
      <div className="mx-auto w-12 h-12 rounded-2xl bg-primary-100 border border-primary-600/15 flex items-center justify-center mb-4">
        <Icon className="w-6 h-6 text-primary-600" />
      </div>
      <h3 className="font-semibold text-ink-900">{title}</h3>
      {description && <p className="text-sm text-ink-500 mt-1 max-w-md mx-auto">{description}</p>}
      {action && <div className="mt-5 flex justify-center">{action}</div>}
    </div>
  )
}

export function ErrorState({
  title = 'No se pudo cargar la información',
  description = 'Revisa tu conexión e inténtalo de nuevo.',
  onRetry,
  className,
}: {
  title?: string
  description?: string
  onRetry?: () => void
  className?: string
}) {
  return (
    <div role="alert" className={clsx('card text-center py-14', className)}>
      <div className="mx-auto w-12 h-12 rounded-2xl bg-danger-500/10 border border-danger-500/20 flex items-center justify-center mb-4">
        <AlertTriangle className="w-6 h-6 text-danger-500" />
      </div>
      <h3 className="font-semibold text-ink-900">{title}</h3>
      <p className="text-sm text-ink-500 mt-1 max-w-md mx-auto">{description}</p>
      {onRetry && (
        <button type="button" onClick={onRetry} className="btn-secondary mt-5 inline-flex items-center gap-2">
          <RefreshCw className="w-4 h-4" /> Reintentar
        </button>
      )}
    </div>
  )
}
