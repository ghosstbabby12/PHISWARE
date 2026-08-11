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
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("roles") val roles: List<String>,
    @SerializedName("points") val points: Int,
    @SerializedName("level") val level: Int
)

data class UrlAnalysisRequest(
    @SerializedName("url") val url: String
)

data class UrlAnalysisResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("originalUrl") val originalUrl: String,
    @SerializedName("domain") val domain: String,
    @SerializedName("riskLevel") val riskLevel: String,
    @SerializedName("riskScore") val riskScore: Double,
    @SerializedName("isPhishing") val isPhishing: Boolean,
    @SerializedName("analysisSource") val analysisSource: String,
    @SerializedName("threats") val threats: List<ThreatResponse>,
    @SerializedName("analysisTimeMs") val analysisTimeMs: Int,
    @SerializedName("analyzedAt") val analyzedAt: String,
    @SerializedName("riskMessage") val riskMessage: String,
    @SerializedName("recommendations") val recommendations: List<String>
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

data class ApiError(
    @SerializedName("status") val status: Int,
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String,
    @SerializedName("path") val path: String?
)
