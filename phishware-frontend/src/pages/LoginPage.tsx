import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { Eye, EyeOff, Lock, User } from 'lucide-react'
import toast from 'react-hot-toast'
import { authService } from '@/services/authService'
import { useAuth } from '@/context/AuthContext'
import { useLang } from '@/i18n/LanguageContext'
import { getErrorMessage, getFieldErrors } from '@/services/api'
import { LoginRequest } from '@/types/auth.types'
import AuthLayout from '@/components/common/AuthLayout'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const { t } = useLang()
  const [showPass, setShowPass] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  const { register, handleSubmit, setError, formState: { errors } } = useForm<LoginRequest>()

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
      toast.success(`${t.auth.welcome}, ${auth.username}!`)
      navigate('/dashboard')
    } catch (err) {
      const fieldErrors = getFieldErrors(err)
      if (fieldErrors.usernameOrEmail) {
        setError('usernameOrEmail', { message: fieldErrors.usernameOrEmail }, { shouldFocus: true })
      }
      if (fieldErrors.password) {
        setError('password', { message: fieldErrors.password }, { shouldFocus: true })
      }
      toast.error(getErrorMessage(err))
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <AuthLayout title={t.auth.loginTitle} subtitle={t.auth.loginSub}>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5" noValidate>
        <div>
          <label htmlFor="usernameOrEmail" className="field-label">{t.auth.user}</label>
          <div className="relative">
            <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-300 pointer-events-none" aria-hidden />
            <input
              id="usernameOrEmail"
              {...register('usernameOrEmail', { required: t.auth.required })}
              className="input-field pl-11"
              placeholder="usuario o email@ejemplo.com"
              autoComplete="username"
            />
          </div>
          {errors.usernameOrEmail && (
            <p role="alert" className="field-error">{errors.usernameOrEmail.message}</p>
          )}
        </div>

        <div>
          <label htmlFor="password" className="field-label">{t.auth.password}</label>
          <div className="relative">
            <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-ink-300 pointer-events-none" aria-hidden />
            <input
              id="password"
              {...register('password', { required: t.auth.required })}
              type={showPass ? 'text' : 'password'}
              className="input-field pl-11 pr-11"
              placeholder="••••••••"
              autoComplete="current-password"
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
          {errors.password && (
            <p role="alert" className="field-error">{errors.password.message}</p>
          )}
        </div>

        <button type="submit" disabled={isLoading} className="btn-primary w-full">
          {isLoading ? t.auth.loggingIn : t.auth.loginBtn}
        </button>

        <p className="text-center text-sm text-ink-500">
          {t.auth.noAccount}{' '}
          <Link to="/register" className="text-primary-700 hover:text-primary-800 font-semibold">
            {t.auth.registerLink}
          </Link>
        </p>
      </form>
    </AuthLayout>
  )
}
