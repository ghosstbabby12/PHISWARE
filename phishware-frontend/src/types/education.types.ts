export type ContentCategory =
  | 'PHISHING_BASICS'
  | 'SOCIAL_ENGINEERING'
  | 'SMISHING'
  | 'VISHING'
  | 'BEST_PRACTICES'
  | 'CASE_STUDIES'
  | 'TOOLS'
  | 'OWASP'
  | 'NIST'

export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'

export interface EducationalContent {
  id: number
  title: string
  slug: string
  category: ContentCategory
  content: string
  summary: string
  difficulty: Difficulty
  readingTimeMin: number
  thumbnailUrl?: string
  tags: string[]
  views: number
  isPublished: boolean
  createdAt: string
}

export interface QuizQuestion {
  id: number
  questionText: string
  questionType: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TRUE_FALSE'
  options: Record<string, string>
  points: number
  orderIndex: number
}

export interface Quiz {
  id: number
  title: string
  description: string
  difficulty: Difficulty
  timeLimitSec: number
  passingScore: number
  pointsReward: number
  questions: QuizQuestion[]
}

export interface QuizAnswerRequest {
  quizId: number
  answers: Record<number, unknown>
  timeTakenSec?: number
}

export interface QuizResult {
  id: number
  score: number
  passed: boolean
  pointsEarned: number
  timeTakenSec: number
  completedAt: string
}
