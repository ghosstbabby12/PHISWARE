package com.phishware.android.data.repository

import com.phishware.android.data.model.*
import com.phishware.android.data.remote.ApiService
import com.phishware.android.data.remote.PageResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhishwareRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun login(usernameOrEmail: String, password: String): Result<AuthResponse> =
        runCatching { apiService.login(LoginRequest(usernameOrEmail, password)) }

    suspend fun register(
        username: String, email: String, password: String,
        firstName: String?, lastName: String?
    ): Result<AuthResponse> = runCatching {
        apiService.register(RegisterRequest(username, email, password, firstName, lastName))
    }

    suspend fun analyzeUrl(url: String): Result<UrlAnalysisResponse> =
        runCatching { apiService.analyzeUrl(UrlAnalysisRequest(url)) }

    suspend fun getHistory(
        page: Int = 0, size: Int = 10, riskLevel: String? = null
    ): Result<PageResponse<UrlAnalysisResponse>> =
        runCatching { apiService.getHistory(page, size, riskLevel) }

    suspend fun getDashboard(): Result<DashboardResponse> =
        runCatching { apiService.getDashboard() }

    suspend fun getUnreadAlertCount(): Result<Long> = runCatching {
        apiService.getUnreadCount()["unreadCount"] ?: 0L
    }

    suspend fun markAlertRead(id: Long): Result<Unit> =
        runCatching { apiService.markAlertRead(id) }

    suspend fun markAllAlertsRead(): Result<Unit> =
        runCatching { apiService.markAllAlertsRead() }
}
