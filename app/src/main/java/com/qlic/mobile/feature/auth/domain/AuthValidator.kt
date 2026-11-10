package com.qlic.mobile.feature.auth.domain

object AuthValidator {
    private val emailPattern = Regex(
        pattern = "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$",
    )

    fun isEmailValid(email: String): Boolean = email == email.trim() && emailPattern.matches(email)

    fun isPasswordValid(password: String): Boolean = password.length in 8..72

    fun validate(request: SignInRequest): AuthError? = when {
        !isEmailValid(request.email) -> AuthError.InvalidEmail
        !isPasswordValid(request.password) -> AuthError.InvalidPassword
        else -> null
    }

    fun validate(request: SignUpRequest): AuthError? = when {
        request.fullName != request.fullName.trim() || request.fullName.length < 2 -> AuthError.InvalidName
        !isEmailValid(request.email) -> AuthError.InvalidEmail
        !isPasswordValid(request.password) -> AuthError.InvalidPassword
        !request.acceptedTerms -> AuthError.TermsRequired
        else -> null
    }
}
