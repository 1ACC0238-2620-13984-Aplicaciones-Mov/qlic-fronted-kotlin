package com.qlic.mobile.feature.auth.domain

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {
    @Test
    fun `sign in rejects invalid input before calling repository`() = runBlocking {
        val repository = FakeAuthRepository()
        val useCase = SignInUseCase(repository)

        val result = useCase(SignInRequest("bad", "short"))

        assertEquals(AuthResult.Failure(AuthError.InvalidEmail), result)
        assertFalse(repository.signInCalled)
    }

    @Test
    fun `sign up delegates valid registration to repository`() = runBlocking {
        val repository = FakeAuthRepository()
        val useCase = SignUpUseCase(repository)
        val request = SignUpRequest("Ana Ruiz", "ana@qlic.com", "valid-pass", acceptedTerms = true)

        val result = useCase(request)

        assertTrue(result is AuthResult.Success)
        assertTrue(repository.signUpCalled)
    }

    private class FakeAuthRepository : AuthRepository {
        var signInCalled = false
        var signUpCalled = false

        override suspend fun signIn(request: SignInRequest): AuthResult<AuthSession> {
            signInCalled = true
            return AuthResult.Success(AuthSession("token", 10L))
        }

        override suspend fun signUp(request: SignUpRequest): AuthResult<AuthSession> {
            signUpCalled = true
            return AuthResult.Success(AuthSession("token", 10L))
        }
    }
}
