import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'

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
    // En login/registro un 401 significa credenciales incorrectas, no sesión expirada:
    // no se redirige para que la pantalla pueda mostrar el error
    const isAuthRequest = error.config?.url?.includes('/auth/')
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

export function getFieldErrors(error: unknown): Record<string, string> {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { fieldErrors?: Record<string, string> } | undefined
    return data?.fieldErrors ?? {}
  }
  return {}
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; fieldErrors?: Record<string, string>; errors?: string[] } | undefined
    if (data?.fieldErrors) {
      return Object.values(data.fieldErrors).join(', ')
    }
    return data?.message ?? error.message
  }
  return 'Ha ocurrido un error inesperado'
}
