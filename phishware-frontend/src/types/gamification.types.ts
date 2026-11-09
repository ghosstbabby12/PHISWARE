export interface Badge {
  id: number
  name: string
  description: string
  iconUrl: string | null
  badgeType: string
  conditionValue: number
  pointsReward: number
  earned: boolean
  earnedAt: string | null
}

export interface Streak {
  currentStreakDays: number
}

export interface AdminStats {
  totalUsers: number
  totalAnalyses: number
  phishingDetected: number
  pendingReports: number
  byRiskLevel: Record<string, number>
  analysesPerDay: { day: string; count: number }[]
}
