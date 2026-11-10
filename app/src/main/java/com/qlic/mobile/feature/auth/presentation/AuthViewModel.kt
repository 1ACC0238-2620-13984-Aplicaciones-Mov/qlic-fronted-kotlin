package com.qlic.mobile.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.qlic.mobile.feature.auth.data.SessionStore
import com.qlic.mobile.feature.auth.domain.AuthResult
import com.qlic.mobile.feature.auth.domain.SignInRequest
import com.qlic.mobile.feature.auth.domain.SignInUseCase
import com.qlic.mobile.feature.auth.domain.SignUpRequest
import com.qlic.mobile.feature.auth.domain.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    sessionStore: SessionStore,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.session.collect { session ->
                mutableState.update { it.copy(isAuthenticated = session != null) }
            }
        }
    }

    fun onEmailChanged(value: String) = mutableState.update { it.copy(email = value, error = null) }
    fun onPasswordChanged(value: String) = mutableState.update { it.copy(password = value, error = null) }
    fun onNameChanged(value: String) = mutableState.update { it.copy(fullName = value, error = null) }
    fun onTermsChanged(value: Boolean) = mutableState.update { it.copy(acceptedTerms = value, error = null) }
    fun onModeChanged(mode: AuthMode) = mutableState.update { AuthUiState(mode = mode) }

    fun submit() {
        val snapshot = mutableState.value
        if (snapshot.isLoading) return
        mutableState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = if (snapshot.mode == AuthMode.SignIn) {
                signInUseCase(SignInRequest(snapshot.email, snapshot.password))
            } else {
                signUpUseCase(
                    SignUpRequest(
                        fullName = snapshot.fullName,
                        email = snapshot.email,
                        password = snapshot.password,
                        acceptedTerms = snapshot.acceptedTerms,
                    ),
                )
            }
            mutableState.update {
                when (result) {
                    is AuthResult.Success -> it.copy(isLoading = false, isAuthenticated = true)
                    is AuthResult.Failure -> it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }

    class Factory(
        private val signInUseCase: SignInUseCase,
        private val signUpUseCase: SignUpUseCase,
        private val sessionStore: SessionStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(signInUseCase, signUpUseCase, sessionStore) as T
    }
}
