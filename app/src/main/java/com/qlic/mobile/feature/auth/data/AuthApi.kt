package com.qlic.mobile.feature.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/authentication/sign-in")
    suspend fun signIn(@Body request: SignInDto): Response<AuthenticationDto>

    @POST("api/v1/authentication/sign-up")
    suspend fun signUp(@Body request: SignUpDto): Response<AccountDto>
}

data class SignInDto(
    val email: String,
    val password: String,
)

data class SignUpDto(
    val fullName: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val acceptTerms: Boolean,
)

data class AccountDto(
    val subscriberId: String,
    val accountId: String,
    val email: String,
    val fullName: String,
    val phone: String?,
    val accountName: String,
    val createdAt: String,
)

data class AuthenticationDto(
    val subscriberId: String,
    val accountId: String,
    val email: String,
    val fullName: String,
    val token: String,
    val tokenType: String,
    val expiresAt: String,
)
