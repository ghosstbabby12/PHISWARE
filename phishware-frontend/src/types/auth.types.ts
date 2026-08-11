export interface LoginRequest {
  usernameOrEmail: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  firstName?: string
  lastName?: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  email: string
  fullName: string
  roles: string[]
  points: number
  level: number
}

export interface User {
  userId: number
  username: string
  email: string
  fullName: string
  roles: string[]
  points: number
  level: number
}
