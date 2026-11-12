package com.qlic.mobile.feature.subscriptions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.subscriptions.domain.PaymentRecord
import com.qlic.mobile.feature.subscriptions.domain.Plan
import com.qlic.mobile.feature.subscriptions.domain.PurchaseResult
import com.qlic.mobile.feature.subscriptions.domain.Subscription
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val subscription: Subscription? = null,
    val payments: List<PaymentRecord> = emptyList(),
    val error: SubscriptionError? = null,
    val isSubmitting: Boolean = false,
    val operation: SubscriptionOperation? = null,
) {
    val purchasablePlans: List<Plan> = Plan.purchasable
}

enum class SubscriptionError { Network, Unauthorized, Server }

enum class SubscriptionOperation { TrialStarted, Purchased, PlanChanged, Cancelled }

class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionUiState(isLoading = true))
    val state: StateFlow<SubscriptionUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    fun startTrial() {
        if (_state.value.isSubmitting) return
        submit(
            request = repository::startTrial,
            operation = SubscriptionOperation.TrialStarted,
            onSuccess = { current, subscription -> current.copy(subscription = subscription) },
        )
    }

    fun purchase(plan: Plan) {
        if (_state.value.isSubmitting || plan !in Plan.purchasable) return
        submit(
            request = { repository.purchase(plan) },
            operation = SubscriptionOperation.Purchased,
            onSuccess = { current, purchase -> current.withPurchase(purchase) },
        )
    }

    fun changePlan(plan: Plan) {
        val subscriptionId = _state.value.subscription?.id ?: return
        if (_state.value.isSubmitting || plan !in Plan.purchasable) return
        submit(
            request = { repository.changePlan(subscriptionId, plan) },
            operation = SubscriptionOperation.PlanChanged,
            onSuccess = { current, subscription -> current.copy(subscription = subscription) },
        )
    }

    fun cancel() {
        val subscriptionId = _state.value.subscription?.id ?: return
        if (_state.value.isSubmitting) return
        submit(
            request = { repository.cancel(subscriptionId) },
            operation = SubscriptionOperation.Cancelled,
            onSuccess = { current, subscription -> current.copy(subscription = subscription) },
        )
    }

    fun clearFeedback() {
        _state.update { it.copy(operation = null, error = null) }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null, operation = null) }
        viewModelScope.launch(dispatcher) {
            val current = repository.getCurrentSubscription()
            val payments = repository.getPaymentHistory()
            val error = sequenceOf(current, payments)
                .firstOrNull { it !is ApiResult.Success }
                ?.toSubscriptionError()

            _state.update { state ->
                state.copy(
                    isLoading = false,
                    subscription = (current as? ApiResult.Success)?.value,
                    payments = (payments as? ApiResult.Success)?.value ?: emptyList(),
                    error = error,
                )
            }
        }
    }

    private fun <T> submit(
        request: suspend () -> ApiResult<T>,
        operation: SubscriptionOperation,
        onSuccess: (SubscriptionUiState, T) -> SubscriptionUiState,
    ) {
        _state.update { it.copy(isSubmitting = true, error = null, operation = null) }
        viewModelScope.launch(dispatcher) {
            when (val result = request()) {
                is ApiResult.Success -> _state.update { current ->
                    onSuccess(current, result.value).copy(
                        isSubmitting = false,
                        operation = operation,
                    )
                }
                else -> _state.update {
                    it.copy(isSubmitting = false, error = result.toSubscriptionError())
                }
            }
        }
    }

    private fun SubscriptionUiState.withPurchase(purchase: PurchaseResult): SubscriptionUiState = copy(
        subscription = purchase.subscription ?: subscription,
        payments = listOf(purchase.payment) + payments.filterNot { it.id == purchase.payment.id },
    )

    private fun ApiResult<*>.toSubscriptionError(): SubscriptionError = when (this) {
        ApiResult.NetworkError -> SubscriptionError.Network
        ApiResult.Unauthorized -> SubscriptionError.Unauthorized
        else -> SubscriptionError.Server
    }
}
