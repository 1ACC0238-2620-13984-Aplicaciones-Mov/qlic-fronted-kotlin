package com.qlic.mobile.feature.consumption.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.consumption.domain.ActivityReport
import com.qlic.mobile.feature.consumption.domain.ActivitySchedule
import com.qlic.mobile.feature.consumption.domain.ConsumptionRepository
import com.qlic.mobile.feature.consumption.domain.isValidTime
import java.time.LocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ConsumptionError { Network, Unauthorized, Server }

data class ConsumptionUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val openingTime: String = "08:00",
    val closingTime: String = "20:00",
    val activeDays: Set<String> = emptySet(),
    val scheduleSaved: Boolean = false,
    val invalidInput: Boolean = false,
    val report: ActivityReport? = null,
    val error: ConsumptionError? = null,
)

class ConsumptionViewModel(
    private val repository: ConsumptionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {
    private val _state = MutableStateFlow(ConsumptionUiState(isLoading = true))
    val state: StateFlow<ConsumptionUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    fun onOpeningChanged(value: String) =
        _state.update { it.copy(openingTime = value, scheduleSaved = false, invalidInput = false) }

    fun onClosingChanged(value: String) =
        _state.update { it.copy(closingTime = value, scheduleSaved = false, invalidInput = false) }

    fun toggleDay(day: String) = _state.update {
        val days = if (day in it.activeDays) it.activeDays - day else it.activeDays + day
        it.copy(activeDays = days, scheduleSaved = false, invalidInput = false)
    }

    fun saveSchedule() {
        val current = _state.value
        if (current.isSaving) return
        val invalid = !isValidTime(current.openingTime) ||
            !isValidTime(current.closingTime) ||
            current.activeDays.isEmpty()
        if (invalid) {
            _state.update { it.copy(invalidInput = true) }
            return
        }
        _state.update { it.copy(isSaving = true, error = null, invalidInput = false) }
        viewModelScope.launch(dispatcher) {
            val result = repository.saveSchedule(
                ActivitySchedule(
                    current.openingTime.trim(),
                    current.closingTime.trim(),
                    current.activeDays,
                ),
            )
            if (result is ApiResult.Success) {
                _state.update { it.copy(isSaving = false, scheduleSaved = true) }
                refreshReport()
            } else {
                _state.update { it.copy(isSaving = false, error = result.toError()) }
            }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch(dispatcher) {
            val schedule = repository.getSchedule()
            val report = repository.getReport(today().minusDays(6).toString(), today().toString())
            // A 404 on the schedule only means that it has not been declared yet.
            val scheduleMissing = schedule is ApiResult.Failure && schedule.statusCode == 404
            val error = when {
                schedule !is ApiResult.Success && !scheduleMissing -> schedule.toError()
                report !is ApiResult.Success -> report.toError()
                else -> null
            }
            _state.update { state ->
                val saved = (schedule as? ApiResult.Success)?.value
                state.copy(
                    isLoading = false,
                    openingTime = saved?.openingTime ?: state.openingTime,
                    closingTime = saved?.closingTime ?: state.closingTime,
                    activeDays = saved?.activeDays ?: state.activeDays,
                    report = (report as? ApiResult.Success)?.value,
                    error = error,
                )
            }
        }
    }

    private suspend fun refreshReport() {
        val report = repository.getReport(today().minusDays(6).toString(), today().toString())
        _state.update { it.copy(report = (report as? ApiResult.Success)?.value ?: it.report) }
    }

    private fun ApiResult<*>.toError(): ConsumptionError = when (this) {
        ApiResult.NetworkError -> ConsumptionError.Network
        ApiResult.Unauthorized -> ConsumptionError.Unauthorized
        else -> ConsumptionError.Server
    }
}
