package com.qlic.mobile.feature.auth.domain

data class SignInRequest(
    val email: String,
    val password: String,
)

data class SignUpRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val acceptedTerms: Boolean,
)

data class AuthSession(
    val accessToken: String,
    val accountId: Long,
)

enum class AuthError {
    InvalidName,
    InvalidEmail,
    InvalidPassword,
    TermsRequired,
    Unauthorized,
    Network,
    Server,
    Unknown,
}

sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val error: AuthError) : AuthResult<Nothing>
}
