package com.phishware.android

import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.model.AuthResponse
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: PhishwareRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        sessionManager = mock()
        viewModel = AuthViewModel(repository, sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login con credenciales vacías debe emitir Error`() = runTest {
        viewModel.login("", "")
        assertIs<AuthViewModel.AuthState.Error>(viewModel.authState.value)
    }

    @Test
    fun `login exitoso debe emitir Success y guardar sesión`() = runTest {
        val mockAuth = AuthResponse(
            accessToken = "token", tokenType = "Bearer", expiresIn = 86400000,
            userId = 1L, username = "testuser", email = "test@test.com",
            fullName = "Test User", roles = listOf("ROLE_USER"),
            points = 0, level = 1
        )

        whenever(repository.login(any(), any())).thenReturn(Result.success(mockAuth))

        viewModel.login("testuser", "Pass@123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertIs<AuthViewModel.AuthState.Success>(viewModel.authState.value)
        verify(sessionManager).saveSession(eq("token"), eq(1L), eq("testuser"), eq("test@test.com"))
    }

    @Test
    fun `login fallido debe emitir Error`() = runTest {
        whenever(repository.login(any(), any())).thenReturn(
            Result.failure(Exception("Credenciales inválidas"))
        )

        viewModel.login("baduser", "badpass")
        testDispatcher.scheduler.advanceUntilIdle()

        assertIs<AuthViewModel.AuthState.Error>(viewModel.authState.value)
    }
}
