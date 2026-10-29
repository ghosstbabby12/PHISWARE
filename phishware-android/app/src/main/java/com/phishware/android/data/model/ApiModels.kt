package com.phishware.android.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("usernameOrEmail") val usernameOrEmail: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
)

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("tokenType") val tokenType: String? = null,
    @SerializedName("expiresIn") val expiresIn: Long = 0,
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("roles") val roles: List<String>? = null,
    @SerializedName("points") val points: Int = 0,
    @SerializedName("level") val level: Int = 1
)

data class UrlAnalysisRequest(
    @SerializedName("url") val url: String
)

data class UrlAnalysisResponse(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("uuid") val uuid: String? = null,
    @SerializedName("originalUrl") val originalUrl: String? = null,
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("riskLevel") val riskLevel: String? = null,
    @SerializedName("riskScore") val riskScore: Double = 0.0,
    @SerializedName("isPhishing") val isPhishing: Boolean = false,
    @SerializedName("analysisSource") val analysisSource: String? = null,
    @SerializedName("threats") val threats: List<ThreatResponse>? = null,
    @SerializedName("analysisTimeMs") val analysisTimeMs: Int? = null,
    @SerializedName("analyzedAt") val analyzedAt: String? = null,
    @SerializedName("riskMessage") val riskMessage: String? = null,
    @SerializedName("recommendations") val recommendations: List<String>? = null
)

data class ThreatResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("threatType") val threatType: String,
    @SerializedName("description") val description: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("source") val source: String
)

data class DashboardResponse(
    @SerializedName("totalAnalyses") val totalAnalyses: Long,
    @SerializedName("threatsDetected") val threatsDetected: Long,
    @SerializedName("safeUrls") val safeUrls: Long,
    @SerializedName("suspiciousUrls") val suspiciousUrls: Long,
    @SerializedName("dangerousUrls") val dangerousUrls: Long,
    @SerializedName("unreadAlerts") val unreadAlerts: Long,
    @SerializedName("userPoints") val userPoints: Int,
    @SerializedName("userLevel") val userLevel: Int
)

data class EducationDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("slug") val slug: String = "",
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("difficulty") val difficulty: String? = null
)

data class QuizQuestionDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("questionText") val questionText: String = "",
    @SerializedName("questionType") val questionType: String = "",
    @SerializedName("options") val options: Map<String, String>? = null,
    @SerializedName("points") val points: Int = 10,
    @SerializedName("orderIndex") val orderIndex: Int = 0
)

data class QuizDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("questions") val questions: List<QuizQuestionDto>? = null,
    @SerializedName("passingScore") val passingScore: Int = 70
)

data class QuizSubmitRequest(
    @SerializedName("quizId") val quizId: Long,
    @SerializedName("answers") val answers: Map<String, Any>,
    @SerializedName("timeTakenSec") val timeTakenSec: Int? = null
)

data class QuizResultDto(
    @SerializedName("score") val score: Int = 0,
    @SerializedName("passed") val passed: Boolean = false,
    @SerializedName("pointsEarned") val pointsEarned: Int = 0
)

data class ReportRequest(
    @SerializedName("url") val url: String,
    @SerializedName("description") val description: String = ""
)

data class ReportDto(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("status") val status: String? = null
)

data class ApiError(
    @SerializedName("status") val status: Int = 0,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("path") val path: String? = null,
    @SerializedName("fieldErrors") val fieldErrors: Map<String, String>? = null
)
