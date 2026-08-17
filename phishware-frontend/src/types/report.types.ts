export type ReportType = 'PHISHING' | 'MALWARE' | 'SUSPICIOUS' | 'SCAM' | 'SPAM'
export type ReportSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type ReportStatus = 'PENDING' | 'REVIEWED' | 'CONFIRMED' | 'REJECTED'

export interface ReportRequest {
  reportedUrl: string
  reportType: ReportType
  description?: string
  severity: ReportSeverity
}

export interface ReportResponse {
  id: number
  reportedUrl: string
  reportType: ReportType
  description: string
  severity: ReportSeverity
  status: ReportStatus
  reportedBy: string
  createdAt: string
}
