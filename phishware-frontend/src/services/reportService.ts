import { api } from './api'
import type { ReportRequest, ReportResponse } from '@/types/report.types'
import type { PageResponse } from '@/types/analysis.types'

export const reportService = {
  async createReport(data: ReportRequest): Promise<ReportResponse> {
    const response = await api.post<ReportResponse>('/reports', data)
    return response.data
  },

  async getMyReports(page = 0, size = 10): Promise<PageResponse<ReportResponse>> {
    const response = await api.get<PageResponse<ReportResponse>>('/reports/my-reports', { params: { page, size } })
    return response.data
  },

  async getPendingReports(page = 0, size = 10): Promise<PageResponse<ReportResponse>> {
    const response = await api.get<PageResponse<ReportResponse>>('/reports/pending', { params: { page, size } })
    return response.data
  },

  async reviewReport(id: number, status: string): Promise<ReportResponse> {
    const response = await api.patch<ReportResponse>(`/reports/${id}/review`, null, { params: { status } })
    return response.data
  },
}
