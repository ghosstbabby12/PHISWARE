import { useEffect, useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import {
  LayoutDashboard, Search, History, BookOpen, Bell, User, LogOut, Flag, ShieldCheck, Settings,
  MoreHorizontal, X, type LucideIcon,
} from 'lucide-react'
import clsx from 'clsx'
import { useAuth } from '@/context/AuthContext'
import { useLang } from '@/i18n/LanguageContext'

interface Item { to: string; icon: LucideIcon; label: string }

/**
 * Navegación flotante.
 * - Escritorio: riel vertical de vidrio a la izquierda, con etiqueta al pasar el cursor.
 * - Móvil: dock inferior con los 4 destinos principales + hoja "Más".
 */
export default function FloatingDock() {
  const { user, logout, isAdmin } = useAuth()
  const { t } = useLang()
  const location = useLocation()
  const [sheetOpen, setSheetOpen] = useState(false)

  useEffect(() => setSheetOpen(false), [location.pathname])

  const primary: Item[] = [
    { to: '/dashboard', icon: LayoutDashboard, label: t.nav.dashboard },
    { to: '/analysis',  icon: Search,          label: t.nav.analysis },
    { to: '/education', icon: BookOpen,        label: t.nav.education },
    { to: '/history',   icon: History,         label: t.nav.history },
  ]
  const secondary: Item[] = [
    { to: '/alerts',   icon: Bell,     label: t.nav.alerts },
    { to: '/reports',  icon: Flag,     label: t.nav.report },
    { to: '/profile',  icon: User,     label: t.nav.profile },
    { to: '/settings', icon: Settings, label: t.nav.settings },
    ...(isAdmin ? [{ to: '/admin', icon: ShieldCheck, label: t.nav.admin }] : []),
  ]
  const initial = user?.username?.[0]?.toUpperCase() ?? 'U'
  const secondaryActive = secondary.some(i => location.pathname.startsWith(i.to))

  return (
    <>
      {/* ── Escritorio: riel flotante ── */}
      <nav
        aria-label={t.fx.mainNav}
        className="hidden lg:flex fixed left-4 top-1/2 -translate-y-1/2 z-40 glass-strong rounded-[2rem] p-2 flex-col items-center gap-1"
      >
        <NavLink to="/dashboard" className="mb-2 mt-1 grid place-items-center" aria-label="PHISHWARE">
          <img src="/phishware-logo.svg" alt="" width={38} height={38} className="rounded-xl" />
        </NavLink>
        {[...primary, ...secondary].map(item => <RailLink key={item.to} {...item} />)}
        <div className="my-1 h-px w-8 bg-ink-200 dark:bg-white/10" />
        <RailButton icon={LogOut} label={t.nav.logout} onClick={logout} danger />
        <NavLink
          to="/profile"
          className="group relative mt-1 grid h-10 w-10 place-items-center rounded-full bg-gradient-to-br from-primary-500 to-primary-700 text-sm font-bold text-white ring-2 ring-white/70 dark:ring-white/10"
          aria-label={`${user?.username} · ${t.common.level} ${user?.level}`}
        >
          {initial}
          <Tooltip>{user?.username} · {t.common.level} {user?.level}</Tooltip>
        </NavLink>
      </nav>

      {/* ── Móvil: dock inferior ── */}
      <nav
        aria-label={t.fx.mainNav}
        className="lg:hidden fixed inset-x-4 bottom-4 z-40 glass-strong rounded-[1.75rem] px-2 py-2 flex items-center justify-between"
      >
        {primary.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => clsx(
              'flex flex-1 flex-col items-center gap-0.5 rounded-2xl py-1.5 text-[10px] font-semibold transition-all',
              isActive ? 'text-primary-700 dark:text-primary-300' : 'text-ink-500 dark:text-ink-300',
            )}
          >
            {({ isActive }) => (
              <>
                <span className={clsx('grid h-8 w-12 place-items-center rounded-full transition-all duration-300',
                  isActive && 'bg-primary-600 text-white shadow-[0_6px_16px_-6px_rgba(121,13,43,0.7)] scale-105')}>
                  <Icon className="h-[18px] w-[18px]" />
                </span>
                <span className="max-w-[4.5rem] truncate">{label}</span>
              </>
            )}
          </NavLink>
        ))}
        <button
          type="button"
          onClick={() => setSheetOpen(true)}
          aria-expanded={sheetOpen}
          className={clsx('flex flex-1 flex-col items-center gap-0.5 rounded-2xl py-1.5 text-[10px] font-semibold',
            secondaryActive ? 'text-primary-700 dark:text-primary-300' : 'text-ink-500 dark:text-ink-300')}
        >
          <span className={clsx('grid h-8 w-12 place-items-center rounded-full', secondaryActive && 'bg-primary-100 dark:bg-primary-600/20')}>
            <MoreHorizontal className="h-[18px] w-[18px]" />
          </span>
          {t.fx.more}
        </button>
      </nav>

      {/* Hoja "Más" (móvil) */}
      {sheetOpen && (
        <div className="lg:hidden fixed inset-0 z-50" role="dialog" aria-modal="true" aria-label={t.fx.menu}>
          <button type="button" className="absolute inset-0 bg-ink-900/40 backdrop-blur-sm animate-fade-in" aria-label={t.common.close} onClick={() => setSheetOpen(false)} />
          <div className="absolute inset-x-3 bottom-3 glass-strong rounded-[2rem] p-4 animate-slide-up">
            <div className="flex items-center justify-between px-2 pb-3">
              <div className="flex items-center gap-3">
                <span className="grid h-10 w-10 place-items-center rounded-full bg-gradient-to-br from-primary-500 to-primary-700 text-sm font-bold text-white">{initial}</span>
                <div>
                  <p className="text-sm font-semibold text-ink-900 dark:text-ink-50">{user?.username}</p>
                  <p className="text-xs text-ink-500 dark:text-ink-300 tabular-nums">{t.common.level} {user?.level} · {user?.points} {t.common.points}</p>
                </div>
              </div>
              <button type="button" onClick={() => setSheetOpen(false)} className="btn-ghost !p-2" aria-label={t.common.close}>
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="grid grid-cols-3 gap-2">
              {secondary.map(({ to, icon: Icon, label }) => (
                <NavLink
                  key={to}
                  to={to}
                  className={({ isActive }) => clsx(
                    'flex flex-col items-center gap-2 rounded-2xl border p-3 text-xs font-semibold transition-all',
                    isActive
                      ? 'border-primary-600/30 bg-primary-600 text-white'
                      : 'border-ink-200/70 bg-white/60 text-ink-700 dark:border-white/10 dark:bg-white/5 dark:text-ink-200',
                  )}
                >
                  <Icon className="h-5 w-5" />
                  {label}
                </NavLink>
              ))}
              <button
                type="button"
                onClick={logout}
                className="flex flex-col items-center gap-2 rounded-2xl border border-danger-500/20 bg-danger-500/5 p-3 text-xs font-semibold text-danger-600"
              >
                <LogOut className="h-5 w-5" />
                {t.nav.logout}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}

function Tooltip({ children }: { children: React.ReactNode }) {
  return (
    <span className="pointer-events-none absolute left-full ml-3 whitespace-nowrap rounded-xl bg-ink-900 px-3 py-1.5 text-xs font-semibold text-white opacity-0 -translate-x-1 shadow-lg transition-all duration-200 group-hover:opacity-100 group-hover:translate-x-0 group-focus-visible:opacity-100 dark:bg-white dark:text-ink-900">
      {children}
    </span>
  )
}

function RailLink({ to, icon: Icon, label }: Item) {
  return (
    <NavLink
      to={to}
      aria-label={label}
      className={({ isActive }) => clsx(
        'group relative grid h-11 w-11 place-items-center rounded-2xl transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-600',
        isActive
          ? 'bg-gradient-to-br from-primary-500 to-primary-700 text-white shadow-[0_8px_18px_-6px_rgba(121,13,43,0.7)]'
          : 'text-ink-500 hover:bg-primary-100 hover:text-primary-700 dark:text-ink-300 dark:hover:bg-white/10 dark:hover:text-white',
      )}
    >
      <Icon className="h-5 w-5 transition-transform duration-200 group-hover:scale-110" />
      <Tooltip>{label}</Tooltip>
    </NavLink>
  )
}

function RailButton({ icon: Icon, label, onClick, danger }: { icon: LucideIcon; label: string; onClick: () => void; danger?: boolean }) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-label={label}
      className={clsx(
        'group relative grid h-11 w-11 place-items-center rounded-2xl transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-600',
        danger ? 'text-danger-500 hover:bg-danger-500/10' : 'text-ink-500 hover:bg-primary-100',
      )}
    >
      <Icon className="h-5 w-5" />
      <Tooltip>{label}</Tooltip>
    </button>
  )
}
