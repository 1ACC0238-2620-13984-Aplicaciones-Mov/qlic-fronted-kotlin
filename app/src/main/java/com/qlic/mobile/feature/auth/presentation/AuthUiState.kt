package com.qlic.mobile.feature.auth.presentation

import com.qlic.mobile.feature.auth.domain.AuthError

enum class AuthMode { SignIn, SignUp }

data class AuthUiState(
    val mode: AuthMode = AuthMode.SignIn,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val acceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: AuthError? = null,
)
