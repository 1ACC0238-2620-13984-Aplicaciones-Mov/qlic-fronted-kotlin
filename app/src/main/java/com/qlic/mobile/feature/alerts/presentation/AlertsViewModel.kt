package com.qlic.mobile.feature.alerts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.alerts.domain.AlertRepository
import com.qlic.mobile.feature.alerts.domain.ConfigureThresholdInput
import com.qlic.mobile.feature.alerts.domain.NotificationPreferenceInput
import com.qlic.mobile.feature.alerts.domain.RecipientInput
import com.qlic.mobile.feature.alerts.domain.Urgency
import java.math.BigDecimal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val repository: AlertRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _state = MutableStateFlow(AlertsUiState(isLoading = true))
    val state: StateFlow<AlertsUiState> = _state.asStateFlow()

    init {
        loadAlerts()
    }

    fun retry() = loadAlerts()

    fun selectFilter(filter: AlertFilter) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun openAlert(alertId: String) {
        _state.update { it.copy(isDetailLoading = true, error = null, formError = null) }
        viewModelScope.launch(dispatcher) {
            when (val result = repository.getAlert(alertId)) {
                is ApiResult.Success -> _state.update {
                    it.copy(isDetailLoading = false, selectedAlert = result.value)
                }
                else -> _state.update {
                    it.copy(isDetailLoading = false, error = result.toAlertsError())
                }
            }
        }
    }

    fun clearSelectedAlert() {
        _state.update { it.copy(selectedAlert = null, formError = null, operationSucceeded = false) }
    }

    fun acknowledgeSelectedAlert(actionTaken: String) {
        val alertId = _state.value.selectedAlert?.id ?: return
        acknowledgeAlert(alertId, actionTaken)
    }

    fun acknowledgeAlert(alertId: String, actionTaken: String) {
        if (_state.value.isSubmitting) return
        if (actionTaken.isBlank()) {
            _state.update { it.copy(formError = AlertFormError.ActionRequired, operationSucceeded = false) }
            return
        }

        submitWrite(
            request = { repository.acknowledgeAlert(alertId, actionTaken.trim()) },
            onSuccess = { current, alert ->
                current.copy(
                    alerts = current.alerts.upsert(alert) { it.id },
                    selectedAlert = if (current.selectedAlert?.id == alert.id) {
                        alert
                    } else {
                        current.selectedAlert
                    },
                )
            },
        )
    }

    fun loadSettings() {
        _state.update { it.copy(isSettingsLoading = true, error = null, formError = null) }
        viewModelScope.launch(dispatcher) {
            val thresholds = repository.getThresholds()
            val preference = repository.getNotificationPreference()
            val recipients = repository.getRecipients()

            val error = sequenceOf(thresholds, preference, recipients)
                .firstOrNull { it !is ApiResult.Success }
                ?.toAlertsError()

            _state.update { current ->
                current.copy(
                    isSettingsLoading = false,
                    thresholds = (thresholds as? ApiResult.Success)?.value ?: current.thresholds,
                    notificationPreference = (preference as? ApiResult.Success)?.value
                        ?: current.notificationPreference,
                    recipients = (recipients as? ApiResult.Success)?.value ?: current.recipients,
                    error = error,
                )
            }
        }
    }

    fun saveThreshold(
        waterPointId: String,
        period: String,
        customLimit: String,
        baseline: String,
    ) {
        if (_state.value.isSubmitting) return
        val customValue = customLimit.trim().takeIf(String::isNotEmpty)?.toPositiveBigDecimalOrNull()
        val baselineValue = baseline.toPositiveBigDecimalOrNull()
        val validationError = when {
            waterPointId.isBlank() -> AlertFormError.WaterPointRequired
            period.isBlank() -> AlertFormError.PeriodRequired
            customLimit.isNotBlank() && customValue == null -> AlertFormError.InvalidCustomLimit
            baselineValue == null -> AlertFormError.InvalidBaseline
            else -> null
        }
        if (validationError != null) {
            _state.update { it.copy(formError = validationError, operationSucceeded = false) }
            return
        }

        submitWrite(
            request = {
                repository.configureThreshold(
                    ConfigureThresholdInput(
                        waterPointId = waterPointId.trim(),
                        period = period.trim(),
                        customLimitLitersPerHour = customValue,
                        baselineLitersPerHour = checkNotNull(baselineValue),
                    ),
                )
            },
            onSuccess = { current, threshold ->
                current.copy(thresholds = current.thresholds.upsert(threshold) { it.id })
            },
        )
    }

    fun saveNotificationPreference(groupingWindow: String, minimumUrgency: Urgency) {
        if (_state.value.isSubmitting) return
        val groupingWindowMinutes = groupingWindow.toIntOrNull()?.takeIf { it > 0 }
        if (groupingWindowMinutes == null || minimumUrgency == Urgency.Unknown) {
            _state.update { it.copy(formError = AlertFormError.InvalidGroupingWindow, operationSucceeded = false) }
            return
        }

        submitWrite(
            request = {
                repository.configureNotificationPreference(
                    NotificationPreferenceInput(groupingWindowMinutes, minimumUrgency),
                )
            },
            onSuccess = { current, preference ->
                current.copy(notificationPreference = preference)
            },
        )
    }

    fun addRecipient(fullName: String, email: String, pushToken: String, enabled: Boolean) {
        if (_state.value.isSubmitting) return
        val error = when {
            fullName.isBlank() -> AlertFormError.InvalidRecipientName
            !email.trim().isEmail() -> AlertFormError.InvalidRecipientEmail
            pushToken.isBlank() -> AlertFormError.PushTokenRequired
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(formError = error, operationSucceeded = false) }
            return
        }

        submitWrite(
            request = {
                repository.addRecipient(
                    RecipientInput(fullName.trim(), email.trim(), pushToken.trim(), enabled),
                )
            },
            onSuccess = { current, recipient ->
                current.copy(recipients = current.recipients.upsert(recipient) { it.id })
            },
        )
    }

    fun toggleRecipient(recipientId: String, enabled: Boolean) {
        if (_state.value.isSubmitting) return
        submitWrite(
            request = { repository.toggleRecipient(recipientId, enabled) },
            onSuccess = { current, recipient ->
                current.copy(recipients = current.recipients.upsert(recipient) { it.id })
            },
        )
    }

    fun clearFeedback() {
        _state.update { it.copy(formError = null, operationSucceeded = false) }
    }

    private fun loadAlerts() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch(dispatcher) {
            when (val result = repository.getAlerts()) {
                is ApiResult.Success -> _state.update {
                    it.copy(isLoading = false, alerts = result.value, error = null)
                }
                ApiResult.Empty -> _state.update {
                    it.copy(isLoading = false, alerts = emptyList(), error = null)
                }
                else -> _state.update {
                    it.copy(isLoading = false, error = result.toAlertsError())
                }
            }
        }
    }

    private fun <T> submitWrite(
        request: suspend () -> ApiResult<T>,
        onSuccess: (AlertsUiState, T) -> AlertsUiState,
    ) {
        _state.update {
            it.copy(isSubmitting = true, formError = null, operationSucceeded = false, error = null)
        }
        viewModelScope.launch(dispatcher) {
            when (val result = request()) {
                is ApiResult.Success -> _state.update { current ->
                    onSuccess(current, result.value).copy(
                        isSubmitting = false,
                        operationSucceeded = true,
                    )
                }
                else -> finishWriteWithError(result)
            }
        }
    }

    private fun finishWriteWithError(result: ApiResult<*>) {
        _state.update { it.copy(isSubmitting = false, error = result.toAlertsError()) }
    }

    private fun ApiResult<*>.toAlertsError(): AlertsError = when (this) {
        ApiResult.NetworkError -> AlertsError.Network
        ApiResult.Unauthorized -> AlertsError.Unauthorized
        else -> AlertsError.Server
    }

    private fun <T> List<T>.upsert(value: T, idOf: (T) -> String): List<T> =
        if (any { idOf(it) == idOf(value) }) {
            map { if (idOf(it) == idOf(value)) value else it }
        } else {
            this + value
        }

    private fun String.toPositiveBigDecimalOrNull(): BigDecimal? =
        trim().toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }

    private fun String.isEmail(): Boolean =
        matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
}
