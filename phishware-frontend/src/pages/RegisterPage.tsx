import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Eye, EyeOff, Lock, Mail, User } from 'lucide-react'
import toast from 'react-hot-toast'
import { useForm } from 'react-hook-form'
import { authService } from '@/services/authService'
import { useAuth } from '@/context/AuthContext'
import { useLang } from '@/i18n/LanguageContext'
import { getErrorMessage } from '@/services/api'
import { RegisterRequest } from '@/types/auth.types'
import AuthLayout from '@/components/common/AuthLayout'

export default function RegisterPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const { t } = useLang()
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
    <AuthLayout title={t.auth.registerTitle} subtitle={t.auth.registerSub}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label htmlFor="firstName" className="field-label">{t.auth.name}</label>
            <input id="firstName" {...register('firstName')} className="input-field" placeholder="Juan" autoComplete="given-name" />
          </div>
          <div>
            <label htmlFor="lastName" className="field-label">{t.auth.lastName}</label>
            <input id="lastName" {...register('lastName')} className="input-field" placeholder="García" autoComplete="family-name" />
          </div>
        </div>

        <div>
          <label htmlFor="username" className="field-label">{t.auth.username}</label>
          <div className="relative">
            <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-300 pointer-events-none" aria-hidden />
            <input
              id="username"
              {...register('username', {
                required: 'El username es requerido',
                minLength: { value: 3, message: 'Mínimo 3 caracteres' },
                pattern: { value: /^[a-zA-Z0-9_]+$/, message: 'Solo letras, números y _' },
              })}
              className="input-field pl-11"
              placeholder="juangarcia"
              autoComplete="username"
            />
          </div>
          {errors.username && <p role="alert" className="field-error">{errors.username.message}</p>}
        </div>

        <div>
          <label htmlFor="email" className="field-label">{t.auth.email}</label>
          <div className="relative">
            <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-300 pointer-events-none" aria-hidden />
            <input
              id="email"
              {...register('email', {
                required: 'El email es requerido',
                pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Email inválido' },
              })}
              type="email"
              className="input-field pl-11"
              placeholder="juan@ejemplo.com"
              autoComplete="email"
            />
          </div>
          {errors.email && <p role="alert" className="field-error">{errors.email.message}</p>}
        </div>

        <div>
          <label htmlFor="password" className="field-label">{t.auth.password}</label>
          <div className="relative">
            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-300 pointer-events-none" aria-hidden />
            <input
              id="password"
              {...register('password', {
                required: 'La contraseña es requerida',
                minLength: { value: 8, message: 'Mínimo 8 caracteres' },
                pattern: {
                  value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9\s])/,
                  message: 'Debe tener mayúscula, minúscula, número y un símbolo (ej. ! @ # < . -)',
                },
              })}
              type={showPass ? 'text' : 'password'}
              className="input-field pl-11 pr-11"
              placeholder="Mín. 8 caracteres"
              autoComplete="new-password"
            />
            <button
              type="button"
              onClick={() => setShowPass(!showPass)}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-ink-300 hover:text-ink-900"
              aria-label={showPass ? t.auth.hidePass : t.auth.showPass}
            >
              {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            </button>
          </div>
          {errors.password && <p role="alert" className="field-error">{errors.password.message}</p>}
        </div>

        <button type="submit" disabled={isLoading} className="btn-primary w-full">
          {isLoading ? t.auth.creating : t.auth.createBtn}
        </button>

        <p className="text-center text-sm text-ink-500">
          {t.auth.hasAccount}{' '}
          <Link to="/login" className="text-primary-700 hover:text-primary-800 font-semibold">
            {t.auth.loginBtn}
          </Link>
        </p>
      </form>
    </AuthLayout>
  )
}
