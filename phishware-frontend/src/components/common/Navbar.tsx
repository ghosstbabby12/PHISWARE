import { Bell } from 'lucide-react'
import { useQuery } from '@tanstack/react-query'
import { dashboardService } from '@/services/dashboardService'
import { Link } from 'react-router-dom'

export default function Navbar() {
  const { data: unread = 0 } = useQuery({
    queryKey: ['unread-alerts'],
    queryFn: dashboardService.getUnreadCount,
    refetchInterval: 30000,
  })

  return (
    <header className="h-16 bg-dark-900/80 backdrop-blur border-b border-slate-700/50 flex items-center justify-end px-6">
      <Link to="/alerts" className="relative p-2 text-slate-400 hover:text-white transition-colors">
        <Bell className="w-5 h-5" />
        {unread > 0 && (
          <span className="absolute top-1 right-1 w-4 h-4 bg-danger-500 text-white text-xs rounded-full flex items-center justify-center font-bold">
            {unread > 9 ? '9+' : unread}
          </span>
        )}
      </Link>
    </header>
  )
}
