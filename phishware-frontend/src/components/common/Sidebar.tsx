import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard, Search, History, BookOpen,
  Bell, User, LogOut, Flag, ShieldCheck
} from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import { useLang } from '@/i18n/LanguageContext'
import clsx from 'clsx'
import Logo from './Logo'
import { Settings } from 'lucide-react'

export default function Sidebar({ onNavigate }: { onNavigate?: () => void }) {
  const { user, logout, isAdmin } = useAuth()
  const { t } = useLang()

  const navItems = [
    { to: '/dashboard',  icon: LayoutDashboard, label: t.nav.dashboard },
    { to: '/analysis',   icon: Search,          label: t.nav.analysis },
    { to: '/history',    icon: History,         label: t.nav.history },
    { to: '/education',  icon: BookOpen,        label: t.nav.education },
    { to: '/alerts',     icon: Bell,            label: t.nav.alerts },
    { to: '/reports',    icon: Flag,            label: t.nav.report },
    { to: '/profile',    icon: User,            label: t.nav.profile },
    { to: '/settings',   icon: Settings,        label: t.nav.settings },
  ]

  return (
    <aside className="w-64 h-full bg-white border-r divider flex flex-col shrink-0">
      <div className="p-6 border-b divider">
        <Logo size={34} />
      </div>

      <nav className="flex-1 p-4 space-y-1 overflow-y-auto" aria-label="Navegación principal">
        {isAdmin && (
          <NavLink
            to="/admin"
            onClick={onNavigate}
            className={({ isActive }) => clsx('nav-link', isActive && 'active')}
          >
            <ShieldCheck className="w-4 h-4" />
            <span>{t.nav.admin}</span>
          </NavLink>
        )}
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onNavigate}
            className={({ isActive }) =>
              clsx('nav-link', isActive && 'active')
            }
          >
            <Icon className="w-4 h-4" />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t divider">
        <div className="flex items-center gap-3 mb-3 px-2">
          <div className="w-9 h-9 rounded-full bg-primary-600 flex items-center justify-center text-sm font-bold text-white shrink-0" aria-hidden>
            {user?.username?.[0]?.toUpperCase() ?? 'U'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-ink-900 truncate">{user?.username}</p>
            <p className="text-xs text-ink-500 tabular-nums">{t.common.level} {user?.level} • {user?.points} {t.common.points}</p>
          </div>
        </div>
        <button
          onClick={() => { onNavigate?.(); logout() }}
          className="nav-link w-full text-danger-600 hover:text-danger-700 hover:bg-danger-500/5"
        >
          <LogOut className="w-4 h-4" />
          <span>{t.nav.logout}</span>
        </button>
      </div>
    </aside>
  )
}
