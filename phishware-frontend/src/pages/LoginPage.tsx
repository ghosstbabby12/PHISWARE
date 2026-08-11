import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { Eye, EyeOff, Lock, Shield, User } from 'lucide-react'
import toast from 'react-hot-toast'
import { authService } from '@/services/authService'
import { useAuth } from '@/context/AuthContext'
import { getErrorMessage } from '@/services/api'
import { LoginRequest } from '@/types/auth.types'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [showPass, setShowPass] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm<LoginRequest>()

  async function onSubmit(data: LoginRequest) {
    setIsLoading(true)
    try {
      const auth = await authService.login(data)
      login({
        userId: auth.userId,
        username: auth.username,
        email: auth.email,
        fullName: auth.fullName,
        roles: auth.roles,
        points: auth.points,
        level: auth.level,
      }, auth.accessToken)
      toast.success(`¡Bienvenido, ${auth.username}!`)
      navigate('/dashboard')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex p-4 bg-primary-600/20 rounded-2xl border border-primary-500/30 mb-4">
            <Shield className="w-10 h-10 text-primary-400" />
          </div>
          <h1 className="text-3xl font-bold text-white">PHISHWARE</h1>
          <p className="text-slate-400 mt-1">Inicia sesión en tu cuenta</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="card space-y-5">
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">
              Usuario o Email
            </label>
            <div className="relative">
              <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                {...register('usernameOrEmail', { required: 'Campo requerido' })}
                className="input-field pl-11"
                placeholder="usuario o email@ejemplo.com"
              />
            </div>
            {errors.usernameOrEmail && (
              <p className="mt-1 text-xs text-danger-400">{errors.usernameOrEmail.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">Contraseña</label>
            <div className="relative">
              <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                {...register('password', { required: 'Campo requerido' })}
                type={showPass ? 'text' : 'password'}
                className="input-field pl-11 pr-11"
                placeholder="••••••••"
              />
              <button
                type="button"
                onClick={() => setShowPass(!showPass)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
              >
                {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {errors.password && (
              <p className="mt-1 text-xs text-danger-400">{errors.password.message}</p>
            )}
          </div>

          <button type="submit" disabled={isLoading} className="btn-primary w-full">
            {isLoading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
          </button>

          <p className="text-center text-sm text-slate-400">
            ¿No tienes cuenta?{' '}
            <Link to="/register" className="text-primary-400 hover:text-primary-300 font-medium">
              Regístrate gratis
            </Link>
          </p>
        </form>
      </div>
    </div>
  )
}
