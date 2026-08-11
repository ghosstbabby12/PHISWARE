import { useQuery } from '@tanstack/react-query'
import {
  Activity, AlertTriangle, CheckCircle, Shield, Star, XCircle
} from 'lucide-react'
import { dashboardService } from '@/services/dashboardService'
import StatCard from '@/components/dashboard/StatCard'
import { useAuth } from '@/context/AuthContext'
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts'

export default function DashboardPage() {
  const { user } = useAuth()

  const { data: dashboard, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: dashboardService.getDashboard,
    refetchInterval: 60000,
  })

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full" />
      </div>
    )
  }

  const chartData = Object.entries(dashboard?.analysesByDay ?? {}).map(([day, count]) => ({
    day: day.slice(5),
    análisis: count,
  }))

  const nextLevelPoints = 100 * Math.pow(2, (user?.level ?? 1))
  const progressPct = Math.min(((user?.points ?? 0) / nextLevelPoints) * 100, 100)

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white">
          Bienvenido, {user?.username} 👋
        </h1>
        <p className="text-slate-400 mt-1">Aquí tienes un resumen de tu actividad de seguridad</p>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard
          title="Total Analizadas"
          value={dashboard?.totalAnalyses ?? 0}
          icon={Activity}
          colorClass="text-primary-400"
        />
        <StatCard
          title="Amenazas Detectadas"
          value={dashboard?.threatsDetected ?? 0}
          icon={Shield}
          colorClass="text-danger-400"
        />
        <StatCard
          title="URLs Seguras"
          value={dashboard?.safeUrls ?? 0}
          icon={CheckCircle}
          colorClass="text-success-500"
        />
        <StatCard
          title="Peligrosas"
          value={dashboard?.dangerousUrls ?? 0}
          icon={XCircle}
          colorClass="text-danger-500"
        />
      </div>

      {/* Chart + Level */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Chart */}
        <div className="xl:col-span-2 card">
          <h2 className="text-base font-semibold text-white mb-4">Análisis últimos 7 días</h2>
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={200}>
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="colorAnalysis" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                <XAxis dataKey="day" stroke="#64748b" tick={{ fontSize: 12 }} />
                <YAxis stroke="#64748b" tick={{ fontSize: 12 }} />
                <Tooltip
                  contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }}
                  labelStyle={{ color: '#f8fafc' }}
                />
                <Area type="monotone" dataKey="análisis" stroke="#3b82f6" fill="url(#colorAnalysis)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-48 flex items-center justify-center text-slate-500">
              <p>Analiza tu primera URL para ver estadísticas</p>
            </div>
          )}
        </div>

        {/* Level card */}
        <div className="card flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <Star className="w-5 h-5 text-warning-500" />
              <h2 className="text-base font-semibold text-white">Tu Progreso</h2>
            </div>
            <p className="text-4xl font-bold text-primary-400 mt-3">Nivel {user?.level}</p>
            <p className="text-slate-400 text-sm mt-1">{user?.points?.toLocaleString()} puntos</p>
          </div>
          <div>
            <div className="flex justify-between text-xs text-slate-400 mb-2">
              <span>Progreso al siguiente nivel</span>
              <span>{progressPct.toFixed(0)}%</span>
            </div>
            <div className="h-2.5 bg-dark-900 rounded-full overflow-hidden">
              <div
                className="h-full bg-primary-500 rounded-full transition-all duration-700"
                style={{ width: `${progressPct}%` }}
              />
            </div>
          </div>
          {dashboard?.suspiciousUrls! > 0 && (
            <div className="flex items-center gap-2 p-3 bg-warning-500/10 rounded-lg border border-warning-500/20 mt-3">
              <AlertTriangle className="w-4 h-4 text-warning-500 flex-shrink-0" />
              <p className="text-xs text-warning-300">
                {dashboard?.suspiciousUrls} URL(s) sospechosas en tu historial
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
