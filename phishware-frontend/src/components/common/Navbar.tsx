import { Bell } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { dashboardService } from '@/services/dashboardService'
import { useLang } from '@/i18n/LanguageContext'
import LanguageToggle from './LanguageToggle'
import ThemeToggle from './ThemeToggle'
import Logo from './Logo'

/** Barra superior flotante de vidrio: identidad (móvil), idioma, tema y alertas. */
export default function Navbar() {
  const { t } = useLang()
  const { data: unread = 0 } = useQuery({
    queryKey: ['unread-alerts'],
    queryFn: dashboardService.getUnreadCount,
    refetchInterval: 30000,
  })

  return (
    <header className="sticky top-3 z-30 mx-auto w-full max-w-6xl px-4 sm:px-6 lg:px-8">
      <div className="glass flex h-14 items-center justify-between rounded-2xl px-3 sm:px-4">
        <Link to="/dashboard" className="lg:hidden" aria-label="PHISHWARE">
          <Logo size={30} />
        </Link>
        <span className="hidden lg:inline text-xs font-semibold uppercase tracking-[0.18em] text-ink-500 dark:text-ink-300">
          PHISHWARE
        </span>
        <div className="flex items-center gap-1">
          <LanguageToggle compact />
          <ThemeToggle compact />
          <Link
            to="/alerts"
            className="relative grid h-10 w-10 place-items-center rounded-xl text-ink-500 transition-colors hover:bg-primary-100 hover:text-primary-700 dark:text-ink-300 dark:hover:bg-white/10 dark:hover:text-white"
            aria-label={unread > 0 ? `${unread} ${t.alerts.title.toLowerCase()}` : t.alerts.title}
          >
            <Bell className={unread > 0 ? 'h-5 w-5 origin-top animate-[wiggle_1s_ease-in-out_2]' : 'h-5 w-5'} />
            {unread > 0 && (
              <span className="absolute top-1 right-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-danger-500 px-1 text-[11px] font-bold text-white ring-2 ring-white dark:ring-dark-900">
                {unread > 9 ? '9+' : unread}
              </span>
            )}
          </Link>
        </div>
      </div>
    </header>
  )
}
