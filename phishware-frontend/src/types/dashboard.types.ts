import { UrlAnalysisResponse } from './analysis.types'

export interface DashboardResponse {
  totalAnalyses: number
  threatsDetected: number
  safeUrls: number
  suspiciousUrls: number
  dangerousUrls: number
  unreadAlerts: number
  userPoints: number
  userLevel: number
  recentAnalyses: UrlAnalysisResponse[]
  threatsByType: Record<string, number>
  analysesByDay: Record<string, number>
}

export interface Alert {
  id: number
  title: string
  message: string
  alertType: string
  severity: string
  isRead: boolean
  recommendations: string[]
  createdAt: string
}
