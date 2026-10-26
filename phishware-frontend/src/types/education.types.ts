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

export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TRUE_FALSE'

export interface QuizQuestion {
  id: number
  questionText: string
  questionType: QuestionType
  options: Record<string, string>
  points: number
  orderIndex: number
}

export interface Quiz {
  id: number
  title: string
  description: string
  contentId?: number
  contentSlug?: string
  contentTitle?: string
  difficulty: Difficulty
  timeLimitSec: number
  passingScore: number
  pointsReward: number
  questions: QuizQuestion[]
  attempts: number
  bestScore: number | null
  passed: boolean
}

export interface QuizAnswerRequest {
  quizId: number
  answers: Record<number, string | string[]>
  timeTakenSec?: number
}

export interface QuestionFeedback {
  questionId: number
  correct: boolean
  selected: string[]
  correctAnswers: string[]
  explanation?: string
}

export interface QuizResult {
  id: number
  quizId: number
  quizTitle: string
  score: number
  passed: boolean
  passingScore: number
  pointsEarned: number
  firstTimePassed: boolean
  correctCount: number
  totalQuestions: number
  attempts: number
  bestScore: number
  timeTakenSec?: number
  completedAt: string
  feedback: QuestionFeedback[]
}

export type ModuleStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'PASSED'

export interface ModuleProgress {
  contentId: number
  slug: string
  title: string
  category: ContentCategory
  difficulty: Difficulty
  quizId: number
  quizTitle: string
  questionCount: number
  passingScore: number
  attempts: number
  bestScore: number | null
  lastScore: number | null
  passed: boolean
  lastAttemptAt: string | null
  status: ModuleStatus
}

export interface AttemptSummary {
  id: number
  quizId: number
  quizTitle: string
  moduleSlug: string | null
  score: number
  passed: boolean
  pointsEarned: number
  completedAt: string
}

export interface LearningProgress {
  totalModules: number
  modulesPassed: number
  modulesAttempted: number
  completionPercentage: number
  averageBestScore: number | null
  totalAttempts: number
  quizPointsEarned: number
  modules: ModuleProgress[]
  recentAttempts: AttemptSummary[]
}
