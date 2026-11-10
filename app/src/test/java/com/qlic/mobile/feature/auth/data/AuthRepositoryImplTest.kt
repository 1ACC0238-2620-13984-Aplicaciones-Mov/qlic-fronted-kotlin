package com.qlic.mobile.feature.auth.data

import com.qlic.mobile.feature.auth.domain.AuthResult
import com.qlic.mobile.feature.auth.domain.AuthSession
import com.qlic.mobile.feature.auth.domain.SignInRequest
import com.qlic.mobile.feature.auth.domain.SignUpRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AuthRepositoryImplTest {
    @Test
    fun `sign in maps backend response and stores session`() = runBlocking {
        val sessionStore = FakeSessionStore()
        val repository = AuthRepositoryImpl(FakeAuthApi(), sessionStore)

        val result = repository.signIn(SignInRequest("ana@qlic.com", "valid-pass"))

        assertTrue(result is AuthResult.Success<*>)
        assertEquals("account-uuid", (result as AuthResult.Success<AuthSession>).value.accountId)
        assertEquals("jwt-token", sessionStore.savedSession?.accessToken)
    }

    @Test
    fun `sign up creates account then signs in to establish session`() = runBlocking {
        val api = FakeAuthApi()
        val repository = AuthRepositoryImpl(api, FakeSessionStore())

        val result = repository.signUp(
            SignUpRequest("Ana Ruiz", "ana@qlic.com", "valid-pass", acceptedTerms = true),
        )

        assertTrue(result is AuthResult.Success<*>)
        assertTrue(api.signUpCalled)
        assertTrue(api.signInCalled)
    }

    private class FakeAuthApi : AuthApi {
        var signUpCalled = false
        var signInCalled = false

        override suspend fun signIn(request: SignInDto): Response<AuthenticationDto> {
            signInCalled = true
            return Response.success(
                AuthenticationDto(
                    subscriberId = "subscriber-uuid",
                    accountId = "account-uuid",
                    email = request.email,
                    fullName = "Ana Ruiz",
                    token = "jwt-token",
                    tokenType = "Bearer",
                    expiresAt = "2026-12-01T00:00:00Z",
                ),
            )
        }

        override suspend fun signUp(request: SignUpDto): Response<AccountDto> {
            signUpCalled = true
            return Response.success(
                201,
                AccountDto(
                    subscriberId = "subscriber-uuid",
                    accountId = "account-uuid",
                    email = request.email,
                    fullName = request.fullName,
                    phone = request.phone,
                    accountName = "Ana's account",
                    createdAt = "2026-11-10T00:00:00Z",
                ),
            )
        }
    }

    private class FakeSessionStore : SessionStore {
        private val state = MutableStateFlow<AuthSession?>(null)
        override val session: Flow<AuthSession?> = state
        val savedSession: AuthSession? get() = state.value

        override suspend fun save(session: AuthSession) {
            state.value = session
        }

        override suspend fun clear() {
            state.value = null
        }
    }
}
