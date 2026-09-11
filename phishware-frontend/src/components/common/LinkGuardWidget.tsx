import { useState, useEffect } from 'react'
import { ShieldAlert, ShieldCheck, ShieldOff, Loader2, X, ExternalLink } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useClipboardGuard } from '@/hooks/useClipboardGuard'

const RISK_STYLES = {
  SAFE:       'border-green-500/40 bg-green-500/10 text-green-400',
  SUSPICIOUS: 'border-yellow-500/40 bg-yellow-500/10 text-yellow-400',
  DANGEROUS:  'border-red-500/40 bg-red-500/10 text-red-400',
}

const RISK_LABELS = {
  SAFE:       '✅ URL segura',
  SUSPICIOUS: '⚠️ URL sospechosa',
  DANGEROUS:  '🚨 URL peligrosa',
}

export default function LinkGuardWidget() {
  const { isActive, toggle, lastAlert, isScanning, clearAlert } = useClipboardGuard()
  const [alertKey, setAlertKey] = useState<number | null>(null)

  // Reset dismissed state whenever a new alert comes in
  useEffect(() => {
    if (lastAlert) setAlertKey(lastAlert.at)
  }, [lastAlert])

  const result = lastAlert?.result
  const showCard = result && alertKey === lastAlert?.at

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col items-end gap-3 pointer-events-none">
      {/* Alert card */}
      {showCard && (
        <div
          className={`pointer-events-auto rounded-xl border p-4 w-72 shadow-2xl backdrop-blur-sm animate-fade-in ${RISK_STYLES[result.riskLevel]}`}
        >
          <div className="flex items-start justify-between gap-3 mb-1">
            <p className="font-semibold text-sm">{RISK_LABELS[result.riskLevel]}</p>
            <button
              onClick={clearAlert}
              className="opacity-60 hover:opacity-100 transition-opacity flex-shrink-0"
              aria-label="Cerrar alerta"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <p className="text-xs opacity-70 truncate mb-2">{result.domain}</p>

          <div className="flex items-center justify-between">
            <span className="text-xs font-medium opacity-80">
              Score: {result.riskScore}/100
            </span>
            {result.riskLevel !== 'SAFE' && (
              <Link
                to="/analysis"
                className="text-xs flex items-center gap-1 underline opacity-70 hover:opacity-100 transition-opacity"
              >
                Ver detalles <ExternalLink className="w-3 h-3" />
              </Link>
            )}
          </div>

          {result.threats.length > 0 && (
            <p className="text-xs mt-2 pt-2 border-t border-current border-opacity-20 opacity-60 line-clamp-2">
              {result.threats[0].description}
            </p>
          )}
        </div>
      )}

      {/* Toggle button */}
      <button
        onClick={toggle}
        className={`pointer-events-auto flex items-center gap-2 px-4 py-2.5 rounded-full font-medium text-sm shadow-xl border transition-all duration-200 ${
          isActive
            ? 'bg-primary-600 border-primary-500 text-white hover:bg-primary-700'
            : 'bg-dark-900/90 border-slate-600 text-slate-400 hover:text-white hover:border-slate-400'
        }`}
        title={isActive ? 'Link Guard activo — haz clic para desactivar' : 'Activar Link Guard'}
      >
        {isScanning ? (
          <Loader2 className="w-4 h-4 animate-spin" />
        ) : isActive ? (
          result?.riskLevel === 'DANGEROUS' ? (
            <ShieldAlert className="w-4 h-4 text-red-300" />
          ) : result?.riskLevel === 'SUSPICIOUS' ? (
            <ShieldAlert className="w-4 h-4 text-yellow-300" />
          ) : (
            <ShieldCheck className="w-4 h-4" />
          )
        ) : (
          <ShieldOff className="w-4 h-4" />
        )}
        <span>
          {isActive
            ? isScanning
              ? 'Escaneando…'
              : 'Link Guard ON'
            : 'Link Guard'}
        </span>
        {isActive && (
          <span className="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse" />
        )}
      </button>
    </div>
  )
}
