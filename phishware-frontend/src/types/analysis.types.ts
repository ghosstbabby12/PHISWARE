export type RiskLevel = 'SAFE' | 'SUSPICIOUS' | 'DANGEROUS'
export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

export interface ThreatResponse {
  id: number
  threatType: string
  description: string
  severity: Severity
  source: string
  detectedAt: string
  persuasionPrinciple?: string | null
  microlessonSlug?: string | null
}

export interface OfficialSeal {
  official: boolean
  brand: string | null
  officialDomain: string | null
}

export interface UrlAnalysisResponse {
  id: number
  uuid: string
  originalUrl: string
  domain: string
  riskLevel: RiskLevel
  riskScore: number
  isPhishing: boolean
  analysisSource: string
  threats: ThreatResponse[]
  analysisTimeMs: number
  analyzedAt: string
  riskMessage: string
  recommendations: string[]
  scoreBreakdown?: Record<string, number>
  officialSeal?: OfficialSeal | null
}

export interface UrlAnalysisRequest {
  url: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}
