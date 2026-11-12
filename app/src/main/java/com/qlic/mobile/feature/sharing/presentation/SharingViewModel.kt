package com.qlic.mobile.feature.sharing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.sharing.domain.AuthorizedUser
import com.qlic.mobile.feature.sharing.domain.Invitation
import com.qlic.mobile.feature.sharing.domain.SharePermission
import com.qlic.mobile.feature.sharing.domain.SharingRepository
import com.qlic.mobile.feature.sharing.domain.isValidEmail
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SharingError { Network, Unauthorized, Forbidden, Conflict, Server }

enum class SharingNotice { Invited, Accepted, Revoked }

data class SharingUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val email: String = "",
    val permission: SharePermission = SharePermission.Viewer,
    val invalidEmail: Boolean = false,
    val issued: List<Invitation> = emptyList(),
    val received: List<Invitation> = emptyList(),
    val authorizedUsers: List<AuthorizedUser> = emptyList(),
    val notice: SharingNotice? = null,
    val error: SharingError? = null,
)

class SharingViewModel(
    private val repository: SharingRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _state = MutableStateFlow(SharingUiState(isLoading = true))
    val state: StateFlow<SharingUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    fun onEmailChanged(value: String) =
        _state.update { it.copy(email = value, invalidEmail = false, notice = null) }

    fun onPermissionChanged(value: SharePermission) = _state.update { it.copy(permission = value) }

    fun invite() {
        val current = _state.value
        if (current.isSubmitting) return
        if (!isValidEmail(current.email)) {
            _state.update { it.copy(invalidEmail = true) }
            return
        }
        perform(SharingNotice.Invited) { repository.invite(current.email, current.permission) }
    }

    fun accept(invitationId: String) = perform(SharingNotice.Accepted) { repository.acceptInvitation(invitationId) }

    fun revoke(authorizedUserId: String) = perform(SharingNotice.Revoked) { repository.revoke(authorizedUserId) }

    private fun perform(notice: SharingNotice, request: suspend () -> ApiResult<*>) {
        if (_state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, error = null, notice = null) }
        viewModelScope.launch(dispatcher) {
            val result = request()
            if (result is ApiResult.Success) {
                _state.update { it.copy(email = if (notice == SharingNotice.Invited) "" else it.email) }
                load(notice)
            } else {
                _state.update { it.copy(isSubmitting = false, error = result.toError()) }
            }
        }
    }

    private fun load(notice: SharingNotice? = null) {
        _state.update { it.copy(isLoading = notice == null, error = null) }
        viewModelScope.launch(dispatcher) {
            val issued = repository.getIssuedInvitations()
            val received = repository.getReceivedInvitations()
            val users = repository.getAuthorizedUsers()
            val failed = sequenceOf(issued, received, users).firstOrNull { it !is ApiResult.Success && it !is ApiResult.Empty }
            _state.update {
                it.copy(
                    isLoading = false,
                    isSubmitting = false,
                    issued = (issued as? ApiResult.Success)?.value ?: emptyList(),
                    received = (received as? ApiResult.Success)?.value ?: emptyList(),
                    authorizedUsers = (users as? ApiResult.Success)?.value ?: emptyList(),
                    notice = notice,
                    error = failed?.toError(),
                )
            }
        }
    }

    private fun ApiResult<*>.toError(): SharingError = when (this) {
        ApiResult.NetworkError -> SharingError.Network
        ApiResult.Unauthorized -> SharingError.Unauthorized
        is ApiResult.Failure -> when (statusCode) {
            403 -> SharingError.Forbidden
            409 -> SharingError.Conflict
            else -> SharingError.Server
        }
        else -> SharingError.Server
    }
}
