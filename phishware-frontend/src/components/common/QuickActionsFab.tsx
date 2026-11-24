import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Plus, Search, QrCode, Flag, ShieldCheck, ShieldOff, ShieldAlert, Loader2, X, ArrowUpRight, type LucideIcon,
} from 'lucide-react'
import clsx from 'clsx'
import { useClipboardGuard } from '@/hooks/useClipboardGuard'
import { useLang } from '@/i18n/LanguageContext'

const RISK_STYLE = {
  SAFE:       { ring: 'border-success-500/40', text: 'text-success-600', bar: 'bg-success-500' },
  SUSPICIOUS: { ring: 'border-warning-500/40', text: 'text-warning-600', bar: 'bg-warning-500' },
  DANGEROUS:  { ring: 'border-danger-500/40',  text: 'text-danger-600',  bar: 'bg-danger-500' },
} as const

/**
 * Botón de acción flotante (speed dial) abajo a la derecha.
 * Reúne las acciones más usadas y el interruptor de Link Guard, con su tarjeta de alerta.
 */
export default function QuickActionsFab() {
  const { t } = useLang()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const { isActive, toggle, lastAlert, isScanning, clearAlert } = useClipboardGuard()

  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && setOpen(false)
    const onClick = (e: MouseEvent) => {
      if (rootRef.current && !rootRef.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('keydown', onKey)
    document.addEventListener('mousedown', onClick)
    return () => {
      document.removeEventListener('keydown', onKey)
      document.removeEventListener('mousedown', onClick)
    }
  }, [open])

  const go = (to: string) => { setOpen(false); navigate(to) }

  const actions: { icon: LucideIcon; label: string; onClick: () => void }[] = [
    { icon: Search, label: t.fx.analyzeUrl, onClick: () => go('/analysis') },
    { icon: QrCode, label: t.fx.scanQr,     onClick: () => go('/analysis?tab=qr') },
    { icon: Flag,   label: t.fx.reportSite, onClick: () => go('/reports') },
  ]

  const result = lastAlert?.result
  const risk = result ? RISK_STYLE[result.riskLevel] : null
  const riskLabel = result
    ? { SAFE: t.fx.guardSafe, SUSPICIOUS: t.fx.guardSuspicious, DANGEROUS: t.fx.guardDangerous }[result.riskLevel]
    : ''
  const GuardIcon = isScanning ? Loader2 : !isActive ? ShieldOff
    : result && result.riskLevel !== 'SAFE' ? ShieldAlert : ShieldCheck

  return (
    <div
      ref={rootRef}
      className="fixed right-4 bottom-28 lg:right-6 lg:bottom-6 z-40 flex flex-col items-end gap-3"
    >
      {/* Tarjeta de alerta de Link Guard */}
      {result && risk && !open && (
        <div className={clsx('glass-strong w-72 rounded-3xl border p-4 animate-scale-in', risk.ring)} role="status" aria-live="polite">
          <div className="flex items-start justify-between gap-3">
            <p className={clsx('flex items-center gap-2 text-sm font-bold', risk.text)}>
              <ShieldAlert className="h-4 w-4" /> {riskLabel}
            </p>
            <button type="button" onClick={clearAlert} className="text-ink-500 hover:text-ink-900 dark:hover:text-white" aria-label={t.common.close}>
              <X className="h-4 w-4" />
            </button>
          </div>
          <p className="mt-1 truncate font-mono text-xs text-ink-500 dark:text-ink-300">{result.domain}</p>
          <div className="mt-3 h-1.5 overflow-hidden rounded-full bg-ink-100 dark:bg-white/10">
            <div className={clsx('h-full rounded-full transition-all duration-700', risk.bar)} style={{ width: `${result.riskScore}%` }} />
          </div>
          <div className="mt-2 flex items-center justify-between text-xs">
            <span className="font-semibold tabular-nums text-ink-700 dark:text-ink-200">{t.fx.score} {result.riskScore}/100</span>
            {result.riskLevel !== 'SAFE' && (
              <Link to="/analysis" className="inline-flex items-center gap-1 font-semibold text-primary-700 hover:underline dark:text-primary-300">
                {t.fx.guardDetails} <ArrowUpRight className="h-3 w-3" />
              </Link>
            )}
          </div>
        </div>
      )}

      {/* Acciones desplegables */}
      {open && (
        <div id="fab-actions" className="flex flex-col items-end gap-2" role="menu" aria-label={t.fx.quickActions}>
          {actions.map(({ icon: Icon, label, onClick }, i) => (
            <button
              key={label}
              type="button"
              role="menuitem"
              onClick={onClick}
              className="fab-item animate-scale-in"
              style={{ animationDelay: `${(actions.length - i) * 40}ms`, animationFillMode: 'backwards' }}
            >
              <span className="grid h-8 w-8 place-items-center rounded-xl bg-primary-100 text-primary-700 dark:bg-primary-600/20 dark:text-primary-300">
                <Icon className="h-4 w-4" />
              </span>
              {label}
            </button>
          ))}
          <button
            type="button"
            role="menuitemcheckbox"
            aria-checked={isActive}
            onClick={toggle}
            className="fab-item animate-scale-in"
            style={{ animationFillMode: 'backwards' }}
          >
            <span className={clsx('grid h-8 w-8 place-items-center rounded-xl',
              isActive ? 'bg-success-500/15 text-success-600' : 'bg-ink-100 text-ink-500 dark:bg-white/10 dark:text-ink-300')}>
              <GuardIcon className={clsx('h-4 w-4', isScanning && 'animate-spin')} />
            </span>
            {isScanning ? t.fx.guardScanning : isActive ? t.fx.guardOn : t.fx.guardOff}
            <span className={clsx('ml-1 h-5 w-9 rounded-full p-0.5 transition-colors', isActive ? 'bg-success-500' : 'bg-ink-300 dark:bg-white/20')} aria-hidden>
              <span className={clsx('block h-4 w-4 rounded-full bg-white shadow transition-transform', isActive && 'translate-x-4')} />
            </span>
          </button>
        </div>
      )}

      {/* Botón principal */}
      <div className="relative">
        {isActive && !open && (
          <span className="absolute inset-0 rounded-full bg-primary-500 animate-ring-pulse" aria-hidden />
        )}
        <button
          type="button"
          onClick={() => setOpen(v => !v)}
          aria-expanded={open}
          aria-controls="fab-actions"
          aria-label={open ? t.fx.closeActions : t.fx.openActions}
          className="fab relative h-14 w-14"
        >
          <Plus className={clsx('h-6 w-6 transition-transform duration-300', open && 'rotate-[135deg]')} />
          {isActive && !open && (
            <span className="absolute -top-0.5 -right-0.5 grid h-5 w-5 place-items-center rounded-full bg-success-500 ring-2 ring-white dark:ring-dark-950">
              <ShieldCheck className="h-3 w-3 text-white" />
            </span>
          )}
        </button>
      </div>
    </div>
  )
}
