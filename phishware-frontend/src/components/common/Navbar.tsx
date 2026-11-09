import { Bell, Menu } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { dashboardService } from '@/services/dashboardService'
import { Link } from 'react-router-dom'
import { useLang } from '@/i18n/LanguageContext'
import LanguageToggle from './LanguageToggle'
import ThemeToggle from './ThemeToggle'

export default function Navbar({ onMenu }: { onMenu: () => void }) {
  const { t } = useLang()
  const { data: unread = 0 } = useQuery({
    queryKey: ['unread-alerts'],
    queryFn: dashboardService.getUnreadCount,
    refetchInterval: 30000,
  })

  return (
    <header className="h-16 bg-white/85 dark:bg-dark-800/85 backdrop-blur border-b divider flex items-center justify-between px-4 sm:px-6 shrink-0">
      <button
        type="button"
        onClick={onMenu}
        className="lg:hidden p-2.5 rounded-xl text-ink-700 dark:text-ink-200 hover:bg-ink-100 dark:hover:bg-dark-800 dark:bg-dark-800 transition-colors"
        aria-label="Abrir menú de navegación"
      >
        <Menu className="w-5 h-5" />
      </button>
      <span className="lg:hidden text-sm font-bold text-ink-900 dark:text-ink-50">PHISHWARE</span>
      <div className="flex items-center gap-1.5">
        <LanguageToggle compact />
        <ThemeToggle compact />
        <Link
          to="/alerts"
          className="relative p-2.5 rounded-xl text-ink-500 dark:text-ink-300 hover:text-primary-700 hover:bg-primary-100 transition-colors"
          aria-label={unread > 0 ? `${unread} ${t.alerts.title.toLowerCase()}` : t.alerts.title}
        >
          <Bell className="w-5 h-5" />
          {unread > 0 && (
            <span className="absolute top-1 right-1 min-w-4 h-4 px-1 bg-danger-500 text-white text-[11px] rounded-full flex items-center justify-center font-bold">
              {unread > 9 ? '9+' : unread}
            </span>
          )}
        </Link>
      </div>
    </header>
  )
}
