package com.phishware.android.data.remote

import com.phishware.android.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("analysis")
    suspend fun analyzeUrl(@Body request: UrlAnalysisRequest): UrlAnalysisResponse

    @GET("analysis/history")
    suspend fun getHistory(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("riskLevel") riskLevel: String? = null
    ): PageResponse<UrlAnalysisResponse>

    @GET("dashboard")
    suspend fun getDashboard(): DashboardResponse

    @GET("alerts")
    suspend fun getAlerts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PageResponse<AlertDto>

    @GET("alerts/unread-count")
    suspend fun getUnreadCount(): Map<String, Long>

    @PATCH("alerts/{id}/read")
    suspend fun markAlertRead(@Path("id") id: Long)

    @PATCH("alerts/read-all")
    suspend fun markAllAlertsRead()
}

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean
)

data class AlertDto(
    val id: Long,
    val title: String,
    val message: String,
    val alertType: String,
    val severity: String,
    val isRead: Boolean,
    val recommendations: List<String>,
    val createdAt: String
)
