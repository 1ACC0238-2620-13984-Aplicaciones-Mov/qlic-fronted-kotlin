package com.qlic.mobile.feature.auth.domain

interface AuthRepository {
    suspend fun signIn(request: SignInRequest): AuthResult<AuthSession>

    suspend fun signUp(request: SignUpRequest): AuthResult<AuthSession>
}
