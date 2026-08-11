import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Eye, EyeOff, Lock, Mail, Shield, User } from 'lucide-react'
import toast from 'react-hot-toast'
import { useForm } from 'react-hook-form'
import { authService } from '@/services/authService'
import { useAuth } from '@/context/AuthContext'
import { getErrorMessage } from '@/services/api'
import { RegisterRequest } from '@/types/auth.types'

export default function RegisterPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [showPass, setShowPass] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm<RegisterRequest>()

  async function onSubmit(data: RegisterRequest) {
    setIsLoading(true)
    try {
      const auth = await authService.register(data)
      login({
        userId: auth.userId,
        username: auth.username,
        email: auth.email,
        fullName: auth.fullName,
        roles: auth.roles,
        points: auth.points,
        level: auth.level,
      }, auth.accessToken)
      toast.success('¡Cuenta creada exitosamente!')
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
        <div className="text-center mb-8">
          <div className="inline-flex p-4 bg-primary-600/20 rounded-2xl border border-primary-500/30 mb-4">
            <Shield className="w-10 h-10 text-primary-400" />
          </div>
          <h1 className="text-3xl font-bold text-white">Crear Cuenta</h1>
          <p className="text-slate-400 mt-1">Únete a PHISHWARE gratuitamente</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="card space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1.5">Nombre</label>
              <input
                {...register('firstName')}
                className="input-field"
                placeholder="Juan"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1.5">Apellido</label>
              <input
                {...register('lastName')}
                className="input-field"
                placeholder="García"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">Usuario</label>
            <div className="relative">
              <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                {...register('username', {
                  required: 'El username es requerido',
                  minLength: { value: 3, message: 'Mínimo 3 caracteres' },
                  pattern: { value: /^[a-zA-Z0-9_]+$/, message: 'Solo letras, números y _' },
                })}
                className="input-field pl-11"
                placeholder="juangarcia"
              />
            </div>
            {errors.username && <p className="mt-1 text-xs text-danger-400">{errors.username.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">Email</label>
            <div className="relative">
              <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                {...register('email', {
                  required: 'El email es requerido',
                  pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Email inválido' },
                })}
                type="email"
                className="input-field pl-11"
                placeholder="juan@ejemplo.com"
              />
            </div>
            {errors.email && <p className="mt-1 text-xs text-danger-400">{errors.email.message}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">Contraseña</label>
            <div className="relative">
              <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                {...register('password', {
                  required: 'La contraseña es requerida',
                  minLength: { value: 8, message: 'Mínimo 8 caracteres' },
                  pattern: {
                    value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/,
                    message: 'Debe tener mayúscula, minúscula, número y símbolo',
                  },
                })}
                type={showPass ? 'text' : 'password'}
                className="input-field pl-11 pr-11"
                placeholder="Min. 8 caracteres"
              />
              <button
                type="button"
                onClick={() => setShowPass(!showPass)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
              >
                {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {errors.password && <p className="mt-1 text-xs text-danger-400">{errors.password.message}</p>}
          </div>

          <button type="submit" disabled={isLoading} className="btn-primary w-full">
            {isLoading ? 'Creando cuenta...' : 'Crear Cuenta Gratis'}
          </button>

          <p className="text-center text-sm text-slate-400">
            ¿Ya tienes cuenta?{' '}
            <Link to="/login" className="text-primary-400 hover:text-primary-300 font-medium">
              Inicia sesión
            </Link>
          </p>
        </form>
      </div>
    </div>
  )
}
