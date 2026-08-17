import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard, Search, History, BookOpen,
  Bell, User, Shield, LogOut, Flag
} from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import clsx from 'clsx'

const navItems = [
  { to: '/dashboard',  icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/analysis',   icon: Search,          label: 'Analizar URL' },
  { to: '/history',    icon: History,         label: 'Historial' },
  { to: '/education',  icon: BookOpen,        label: 'Educación' },
  { to: '/alerts',     icon: Bell,            label: 'Alertas' },
  { to: '/reports',    icon: Flag,            label: 'Reportar' },
  { to: '/profile',    icon: User,            label: 'Perfil' },
]

export default function Sidebar() {
  const { user, logout } = useAuth()

  return (
    <aside className="w-64 bg-dark-900 border-r border-slate-700/50 flex flex-col">
      {/* Logo */}
      <div className="p-6 border-b border-slate-700/50">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary-600/20 rounded-lg border border-primary-500/30">
            <Shield className="w-6 h-6 text-primary-400" />
          </div>
          <div>
            <h1 className="font-bold text-lg text-white tracking-tight">PHISHWARE</h1>
            <p className="text-xs text-slate-500">Protección Inteligente</p>
          </div>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 p-4 space-y-1">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              clsx('nav-link', isActive && 'active')
            }
          >
            <Icon className="w-4 h-4" />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>

      {/* User & Logout */}
      <div className="p-4 border-t border-slate-700/50">
        <div className="flex items-center gap-3 mb-3 px-2">
          <div className="w-8 h-8 rounded-full bg-primary-600 flex items-center justify-center text-sm font-bold text-white">
            {user?.username?.[0]?.toUpperCase() ?? 'U'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-white truncate">{user?.username}</p>
            <p className="text-xs text-slate-500">Nivel {user?.level} • {user?.points} pts</p>
          </div>
        </div>
        <button
          onClick={logout}
          className="nav-link w-full text-danger-400 hover:text-danger-300 hover:bg-danger-500/10"
        >
          <LogOut className="w-4 h-4" />
          <span>Cerrar sesión</span>
        </button>
      </div>
    </aside>
  )
}
