import { api } from './api'
import { AdminStats, Badge, Streak } from '@/types/gamification.types'

export const gamificationService = {
  async getBadges(): Promise<Badge[]> {
    const { data } = await api.get<Badge[]>('/gamification/badges')
    return data
  },
  async getStreak(): Promise<Streak> {
    const { data } = await api.get<Streak>('/gamification/streak')
    return data
  },
}

export const adminService = {
  async getStats(): Promise<AdminStats> {
    const { data } = await api.get<AdminStats>('/admin/stats')
    return data
  },
}
