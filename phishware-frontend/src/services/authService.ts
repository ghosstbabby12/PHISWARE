import { api } from './api'
import { AuthResponse, LoginRequest, RegisterRequest } from '@/types/auth.types'

export const authService = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', data)
    return response.data
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/register', data)
    return response.data
  },

  saveSession(auth: AuthResponse): void {
    localStorage.setItem('phishware_token', auth.accessToken)
    localStorage.setItem('phishware_user', JSON.stringify({
      userId: auth.userId,
      username: auth.username,
      email: auth.email,
      fullName: auth.fullName,
      roles: auth.roles,
      points: auth.points,
      level: auth.level,
    }))
  },

  clearSession(): void {
    localStorage.removeItem('phishware_token')
    localStorage.removeItem('phishware_user')
  },

  getStoredUser() {
    const raw = localStorage.getItem('phishware_user')
    return raw ? JSON.parse(raw) : null
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem('phishware_token')
  },
}
