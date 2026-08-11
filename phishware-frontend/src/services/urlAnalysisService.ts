import { api } from './api'
import { PageResponse, UrlAnalysisRequest, UrlAnalysisResponse } from '@/types/analysis.types'

export const urlAnalysisService = {
  async analyze(data: UrlAnalysisRequest): Promise<UrlAnalysisResponse> {
    const response = await api.post<UrlAnalysisResponse>('/analysis', data)
    return response.data
  },

  async getHistory(page = 0, size = 10, riskLevel?: string): Promise<PageResponse<UrlAnalysisResponse>> {
    const params: Record<string, unknown> = { page, size, sort: 'analyzedAt,desc' }
    if (riskLevel) params.riskLevel = riskLevel
    const response = await api.get<PageResponse<UrlAnalysisResponse>>('/analysis/history', { params })
    return response.data
  },
}
