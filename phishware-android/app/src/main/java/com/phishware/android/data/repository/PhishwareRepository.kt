package com.phishware.android.data.repository

import com.google.gson.Gson
import com.phishware.android.data.model.*
import com.phishware.android.data.remote.ApiService
import com.phishware.android.data.remote.PageResponse
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhishwareRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun login(usernameOrEmail: String, password: String): Result<AuthResponse> =
        call { apiService.login(LoginRequest(usernameOrEmail, password)) }

    suspend fun register(
        username: String, email: String, password: String,
        firstName: String?, lastName: String?
    ): Result<AuthResponse> = call {
        apiService.register(RegisterRequest(username, email, password, firstName, lastName))
    }

    suspend fun analyzeUrl(url: String): Result<UrlAnalysisResponse> =
        call { apiService.analyzeUrl(UrlAnalysisRequest(url)) }

    suspend fun getHistory(
        page: Int = 0, size: Int = 10, riskLevel: String? = null
    ): Result<PageResponse<UrlAnalysisResponse>> =
        call { apiService.getHistory(page, size, riskLevel) }

    suspend fun getDashboard(): Result<DashboardResponse> =
        call { apiService.getDashboard() }

    suspend fun getUnreadAlertCount(): Result<Long> = call {
        apiService.getUnreadCount()["unreadCount"] ?: 0L
    }

    suspend fun getAlerts(page: Int = 0, size: Int = 20) =
        call { apiService.getAlerts(page, size) }

    suspend fun markAlertRead(id: Long): Result<Unit> =
        call { apiService.markAlertRead(id) }

    suspend fun markAllAlertsRead(): Result<Unit> =
        call { apiService.markAllAlertsRead() }

    suspend fun getEducation(page: Int = 0, size: Int = 20) =
        call { apiService.getEducation(page, size) }

    suspend fun getEducationDetail(slug: String) =
        call { apiService.getEducationDetail(slug) }

    suspend fun getQuizByModule(slug: String) =
        call { apiService.getQuizByModule(slug) }

    suspend fun submitQuiz(quizId: Long, answers: Map<String, Any>) =
        call { apiService.submitQuiz(QuizSubmitRequest(quizId, answers)) }

    suspend fun reportUrl(url: String, description: String = "Reportado desde Android") =
        call { apiService.reportUrl(ReportRequest(url, description)) }

    private suspend fun <T> call(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(Exception(e.toUserMessage()))
    }

    private fun Throwable.toUserMessage(): String {
        if (this is HttpException) {
            val raw = runCatching { response()?.errorBody()?.string() }.getOrNull()
            if (!raw.isNullOrBlank()) {
                val parsed = runCatching { Gson().fromJson(raw, ApiError::class.java) }.getOrNull()
                val fields = parsed?.fieldErrors?.values?.filter { it.isNotBlank() }?.joinToString("\n")
                if (!fields.isNullOrBlank()) return fields
                val serverMessage = parsed?.message
                if (!serverMessage.isNullOrBlank()) return serverMessage
            }
            return when (code()) {
                401 -> "Credenciales inválidas"
                403 -> "Acceso denegado"
                else -> "Error del servidor (${code()})"
            }
        }
        if (this is IOException) {
            return "Sin conexión con el servidor. Enciende el backend y deja el celular por USB."
        }
        return message ?: "Error inesperado"
    }
}
