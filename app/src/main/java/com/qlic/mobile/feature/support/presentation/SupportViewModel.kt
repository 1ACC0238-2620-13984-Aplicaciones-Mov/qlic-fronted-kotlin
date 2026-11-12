package com.qlic.mobile.feature.support.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.support.domain.Incident
import com.qlic.mobile.feature.support.domain.MAX_INCIDENT_DESCRIPTION
import com.qlic.mobile.feature.support.domain.RegisterIncidentInput
import com.qlic.mobile.feature.support.domain.SupportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SupportError { Network, Unauthorized, Server }

enum class SupportFormError { EmptyDescription, TooLong, NoDevice }

data class SupportUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val incidents: List<Incident> = emptyList(),
    val description: String = "",
    val selectedDeviceId: String? = null,
    val formError: SupportFormError? = null,
    val registered: Boolean = false,
    val error: SupportError? = null,
)

class SupportViewModel(
    private val repository: SupportRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _state = MutableStateFlow(SupportUiState(isLoading = true))
    val state: StateFlow<SupportUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    fun onDescriptionChanged(value: String) =
        _state.update { it.copy(description = value, formError = null, registered = false) }

    fun onDeviceSelected(deviceId: String) =
        _state.update { it.copy(selectedDeviceId = deviceId, formError = null, registered = false) }

    fun submit() {
        val current = _state.value
        if (current.isSubmitting) return
        val description = current.description.trim()
        val deviceId = current.selectedDeviceId
        val formError = when {
            description.isEmpty() -> SupportFormError.EmptyDescription
            description.length > MAX_INCIDENT_DESCRIPTION -> SupportFormError.TooLong
            deviceId == null -> SupportFormError.NoDevice
            else -> null
        }
        if (formError != null || deviceId == null) {
            _state.update { it.copy(formError = formError) }
            return
        }
        _state.update { it.copy(isSubmitting = true, error = null, registered = false) }
        viewModelScope.launch(dispatcher) {
            when (val result = repository.registerIncident(RegisterIncidentInput(description, deviceId))) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        isSubmitting = false,
                        registered = true,
                        description = "",
                        incidents = listOf(result.value) + it.incidents.filterNot { i -> i.id == result.value.id },
                    )
                }
                else -> _state.update { it.copy(isSubmitting = false, error = result.toError()) }
            }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch(dispatcher) {
            val result = repository.getIncidents()
            _state.update {
                it.copy(
                    isLoading = false,
                    incidents = (result as? ApiResult.Success)?.value ?: emptyList(),
                    error = if (result is ApiResult.Success || result is ApiResult.Empty) null else result.toError(),
                )
            }
        }
    }

    private fun ApiResult<*>.toError(): SupportError = when (this) {
        ApiResult.NetworkError -> SupportError.Network
        ApiResult.Unauthorized -> SupportError.Unauthorized
        else -> SupportError.Server
    }
}
