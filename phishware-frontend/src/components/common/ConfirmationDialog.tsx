import { useEffect, useRef } from 'react'
import { AlertTriangle } from 'lucide-react'

interface Props {
  open: boolean
  title: string
  description?: string
  confirmLabel?: string
  cancelLabel?: string
  onConfirm: () => void
  onCancel: () => void
}

/** Dialogo accesible con foco inicial y cierre con Escape. */
export default function ConfirmationDialog({
  open,
  title,
  description,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  onConfirm,
  onCancel,
}: Props) {
  const confirmRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!open) return
    confirmRef.current?.focus()
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onCancel()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open, onCancel])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-ink-900/40"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-title"
      onClick={onCancel}
    >
      <div
        className="card w-full max-w-md"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start gap-3">
          <div className="p-2 bg-warning-500/10 rounded-xl border border-warning-500/25 shrink-0">
            <AlertTriangle className="w-5 h-5 text-warning-600" />
          </div>
          <div>
            <h2 id="confirm-title" className="font-semibold text-ink-900">{title}</h2>
            {description && <p className="text-sm text-ink-500 mt-1">{description}</p>}
          </div>
        </div>
        <div className="flex justify-end gap-2 mt-6">
          <button type="button" onClick={onCancel} className="btn-secondary">
            {cancelLabel}
          </button>
          <button type="button" ref={confirmRef} onClick={onConfirm} className="btn-primary">
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
