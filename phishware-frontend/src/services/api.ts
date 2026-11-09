import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { STRINGS, type Lang } from '@/i18n/translations'

const BASE_URL = import.meta.env.VITE_API_URL ?? '/api'

export const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
})

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('phishware_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    const status = error.response?.status
    const isAuthRequest = error.config?.url?.includes('/auth/')
    // En login/registro un 401 puede ser credenciales incorrectas... o un token
    // obsoleto guardado (ej. DB recreada): se limpia para que el reintento vaya sin Authorization
    if (isAuthRequest && status === 401) {
      localStorage.removeItem('phishware_token')
      localStorage.removeItem('phishware_user')
    }
    // 401 = token expired/invalid (after AuthenticationEntryPoint fix)
    // 403 on non-admin endpoint = old backend behavior with expired token
    if (!isAuthRequest && (status === 401 || (status === 403 && !error.config?.url?.includes('/admin')))) {
      localStorage.removeItem('phishware_token')
      localStorage.removeItem('phishware_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

function currentLang(): Lang {
  try {
    const stored = localStorage.getItem('phishware_lang')
    return stored === 'en' ? 'en' : 'es'
  } catch {
    return 'es'
  }
}

export function getFieldErrors(error: unknown): Record<string, string> {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { fieldErrors?: Record<string, string> } | undefined
    return data?.fieldErrors ?? {}
  }
  return {}
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { code?: string; message?: string; fieldErrors?: Record<string, string>; errors?: string[] } | undefined
    const t = STRINGS[currentLang()]
    if (data?.code && data.code in t.errors) {
      return t.errors[data.code as keyof typeof t.errors]
    }
    if (data?.fieldErrors) {
      return Object.values(data.fieldErrors).join(', ')
    }
    return data?.message ?? error.message
  }
  return STRINGS[currentLang()].errors.default
}
