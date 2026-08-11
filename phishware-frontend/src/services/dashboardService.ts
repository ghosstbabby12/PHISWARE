import { api } from './api'
import { Alert, DashboardResponse } from '@/types/dashboard.types'
import { PageResponse } from '@/types/analysis.types'

export const dashboardService = {
  async getDashboard(): Promise<DashboardResponse> {
    const response = await api.get<DashboardResponse>('/dashboard')
    return response.data
  },

  async getAlerts(page = 0, size = 20): Promise<PageResponse<Alert>> {
    const response = await api.get<PageResponse<Alert>>('/alerts', {
      params: { page, size },
    })
    return response.data
  },

  async getUnreadCount(): Promise<number> {
    const response = await api.get<{ unreadCount: number }>('/alerts/unread-count')
    return response.data.unreadCount
  },

  async markAlertRead(id: number): Promise<void> {
    await api.patch(`/alerts/${id}/read`)
  },

  async markAllAlertsRead(): Promise<void> {
    await api.patch('/alerts/read-all')
  },
}
