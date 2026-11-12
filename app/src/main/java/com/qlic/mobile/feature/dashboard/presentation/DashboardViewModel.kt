package com.qlic.mobile.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.alerts.domain.AlertRepository
import com.qlic.mobile.feature.alerts.domain.AlertState
import com.qlic.mobile.feature.alerts.domain.Urgency
import com.qlic.mobile.feature.devices.domain.DeviceRepository
import com.qlic.mobile.feature.devices.domain.DeviceStatus
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val deviceRepository: DeviceRepository,
    private val alertRepository: AlertRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        retryDevices()
        retryAlerts()
        retrySubscription()
    }

    fun retryDevices() {
        _state.update { it.copy(devices = it.devices.copy(isLoading = true, error = null)) }
        viewModelScope.launch(dispatcher) {
            when (val result = deviceRepository.getDevices()) {
                is ApiResult.Success -> {
                    val devices = result.value
                    _state.update {
                        it.copy(
                            devices = DashboardSection(
                                isLoading = false,
                                data = DeviceMetrics(
                                    total = devices.size,
                                    online = devices.count { device -> device.status == DeviceStatus.Online },
                                    attentionRequired = devices.count { device ->
                                        when (device.status) {
                                            DeviceStatus.Offline,
                                            DeviceStatus.LowBattery,
                                            DeviceStatus.Unknown -> true
                                            DeviceStatus.Online,
                                            DeviceStatus.Calibrating -> false
                                        }
                                    },
                                ),
                            ),
                        )
                    }
                }
                ApiResult.Empty -> _state.update {
                    it.copy(devices = DashboardSection(false, DeviceMetrics(0, 0, 0)))
                }
                else -> _state.update {
                    it.copy(devices = DashboardSection(false, error = result.toDashboardError()))
                }
            }
        }
    }

    fun retryAlerts() {
        _state.update { it.copy(alerts = it.alerts.copy(isLoading = true, error = null)) }
        viewModelScope.launch(dispatcher) {
            when (val result = alertRepository.getAlerts()) {
                is ApiResult.Success -> {
                    val alerts = result.value
                    _state.update {
                        it.copy(
                            alerts = DashboardSection(
                                isLoading = false,
                                data = AlertMetrics(
                                    open = alerts.count { alert -> alert.state == AlertState.Active },
                                    critical = alerts.count { alert -> alert.urgency == Urgency.Critical },
                                    acknowledged = alerts.count { alert -> alert.state == AlertState.Acknowledged },
                                ),
                            ),
                        )
                    }
                }
                ApiResult.Empty -> _state.update {
                    it.copy(alerts = DashboardSection(false, AlertMetrics(0, 0, 0)))
                }
                else -> _state.update {
                    it.copy(alerts = DashboardSection(false, error = result.toDashboardError()))
                }
            }
        }
    }

    fun retrySubscription() {
        _state.update { it.copy(subscription = it.subscription.copy(isLoading = true, error = null)) }
        viewModelScope.launch(dispatcher) {
            when (val result = subscriptionRepository.getCurrentSubscription()) {
                is ApiResult.Success -> {
                    val subscription = result.value
                    _state.update {
                        it.copy(
                            subscription = DashboardSection(
                                isLoading = false,
                                data = SubscriptionMetrics(
                                    currentPlan = subscription?.currentPlan,
                                    status = subscription?.status,
                                    paidFeaturesEnabled = subscription?.paidFeaturesEnabled ?: false,
                                    maxWaterPointsAllowed = subscription?.maxWaterPointsAllowed ?: 0,
                                ),
                            ),
                        )
                    }
                }
                ApiResult.Empty -> _state.update {
                    it.copy(subscription = DashboardSection(false, SubscriptionMetrics(null, null, false, 0)))
                }
                else -> _state.update {
                    it.copy(subscription = DashboardSection(false, error = result.toDashboardError()))
                }
            }
        }
    }

    private fun ApiResult<*>.toDashboardError(): DashboardError = when (this) {
        ApiResult.NetworkError -> DashboardError.Network
        ApiResult.Unauthorized -> DashboardError.Unauthorized
        else -> DashboardError.Server
    }
}
