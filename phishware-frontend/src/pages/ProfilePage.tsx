import { useAuth } from '@/context/AuthContext'
import { User, Mail, Star, Shield, Activity } from 'lucide-react'

export default function ProfilePage() {
  const { user, isAdmin } = useAuth()

  const levelThresholds = [0, 100, 250, 500, 800, 1200, 1700, 2300, 3000, 4000]
  const currentThreshold = levelThresholds[(user?.level ?? 1) - 1] ?? 0
  const nextThreshold = levelThresholds[user?.level ?? 1] ?? ((user?.points ?? 0) + 1)
  const denominator = nextThreshold - currentThreshold
  const progress = denominator > 0 ? (((user?.points ?? 0) - currentThreshold) / denominator) * 100 : 100

  return (
    <div className="max-w-2xl mx-auto space-y-6 animate-fade-in">
      <h1 className="text-2xl font-bold text-white">Mi Perfil</h1>

      {/* Profile card */}
      <div className="card">
        <div className="flex items-center gap-6">
          <div className="w-20 h-20 rounded-full bg-primary-600 flex items-center justify-center text-3xl font-bold text-white flex-shrink-0">
            {user?.username?.[0]?.toUpperCase()}
          </div>
          <div>
            <h2 className="text-xl font-bold text-white">{user?.fullName || user?.username}</h2>
            <p className="text-slate-400 text-sm">@{user?.username}</p>
            <div className="flex gap-2 mt-2">
              {user?.roles.map(role => (
                <span key={role} className="text-xs px-2 py-0.5 rounded-full bg-primary-600/20 text-primary-400 border border-primary-500/30">
                  {role.replace('ROLE_', '')}
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Info */}
      <div className="card space-y-4">
        <h3 className="font-semibold text-white">Información de cuenta</h3>
        <div className="flex items-center gap-3 text-slate-300">
          <User className="w-4 h-4 text-slate-500" />
          <span className="text-sm">{user?.username}</span>
        </div>
        <div className="flex items-center gap-3 text-slate-300">
          <Mail className="w-4 h-4 text-slate-500" />
          <span className="text-sm">{user?.email}</span>
        </div>
        {isAdmin && (
          <div className="flex items-center gap-3 text-warning-400">
            <Shield className="w-4 h-4" />
            <span className="text-sm font-medium">Administrador del Sistema</span>
          </div>
        )}
      </div>

      {/* Gamification */}
      <div className="card space-y-4">
        <h3 className="font-semibold text-white flex items-center gap-2">
          <Star className="w-5 h-5 text-warning-500" />
          Progreso y Gamificación
        </h3>

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-dark-900 rounded-lg p-4 text-center">
            <p className="text-3xl font-bold text-primary-400">{user?.level}</p>
            <p className="text-sm text-slate-400 mt-1">Nivel Actual</p>
          </div>
          <div className="bg-dark-900 rounded-lg p-4 text-center">
            <p className="text-3xl font-bold text-warning-500">{user?.points?.toLocaleString()}</p>
            <p className="text-sm text-slate-400 mt-1">Puntos Totales</p>
          </div>
        </div>

        <div>
          <div className="flex justify-between text-sm text-slate-400 mb-2">
            <span>Progreso al Nivel {(user?.level ?? 1) + 1}</span>
            <span>{Math.min(Math.round(progress), 100)}%</span>
          </div>
          <div className="h-3 bg-dark-900 rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-primary-500 to-primary-400 rounded-full transition-all duration-700"
              style={{ width: `${Math.min(progress, 100)}%` }}
            />
          </div>
          <p className="text-xs text-slate-500 mt-1.5">
            {nextThreshold - (user?.points ?? 0)} puntos para el siguiente nivel
          </p>
        </div>
      </div>

      {/* Tips */}
      <div className="card border-primary-500/20">
        <h3 className="font-semibold text-slate-300 flex items-center gap-2 mb-3">
          <Activity className="w-4 h-4 text-primary-400" />
          Cómo ganar puntos
        </h3>
        <ul className="space-y-2 text-sm text-slate-400">
          <li className="flex justify-between">
            <span>Analizar una URL</span>
            <span className="text-primary-400 font-mono">+5 pts</span>
          </li>
          <li className="flex justify-between">
            <span>Detectar una amenaza real</span>
            <span className="text-primary-400 font-mono">+15 pts</span>
          </li>
          <li className="flex justify-between">
            <span>Completar un quiz</span>
            <span className="text-primary-400 font-mono">+50 pts</span>
          </li>
          <li className="flex justify-between">
            <span>Obtener puntuación perfecta en quiz</span>
            <span className="text-primary-400 font-mono">+100 pts</span>
          </li>
        </ul>
      </div>
    </div>
  )
}
