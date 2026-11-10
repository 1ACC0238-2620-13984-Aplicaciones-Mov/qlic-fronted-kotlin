package com.qlic.mobile.feature.auth.domain

class SignInUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(request: SignInRequest): AuthResult<AuthSession> {
        val validationError = AuthValidator.validate(request)
        return if (validationError == null) {
            repository.signIn(request)
        } else {
            AuthResult.Failure(validationError)
        }
    }
}

class SignUpUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(request: SignUpRequest): AuthResult<AuthSession> {
        val validationError = AuthValidator.validate(request)
        return if (validationError == null) {
            repository.signUp(request)
        } else {
            AuthResult.Failure(validationError)
        }
    }
}
