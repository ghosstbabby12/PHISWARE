import { api } from './api'
import { EducationalContent, Quiz, QuizAnswerRequest, QuizResult } from '@/types/education.types'
import { PageResponse } from '@/types/analysis.types'

export const educationService = {
  async getAll(params?: {
    category?: string
    difficulty?: string
    page?: number
    size?: number
  }): Promise<PageResponse<EducationalContent>> {
    const response = await api.get<PageResponse<EducationalContent>>('/education', { params })
    return response.data
  },

  async getBySlug(slug: string): Promise<EducationalContent> {
    const response = await api.get<EducationalContent>(`/education/${slug}`)
    return response.data
  },

  async getQuizzes(): Promise<PageResponse<Quiz>> {
    const response = await api.get<PageResponse<Quiz>>('/quiz')
    return response.data
  },

  async getQuiz(id: number): Promise<Quiz> {
    const response = await api.get<Quiz>(`/quiz/${id}`)
    return response.data
  },

  async submitQuiz(data: QuizAnswerRequest): Promise<QuizResult> {
    const response = await api.post<QuizResult>('/quiz/submit', data)
    return response.data
  },
}
