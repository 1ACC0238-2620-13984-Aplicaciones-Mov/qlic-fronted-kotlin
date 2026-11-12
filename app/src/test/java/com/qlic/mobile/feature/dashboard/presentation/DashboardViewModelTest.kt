package com.qlic.mobile.feature.dashboard.presentation

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.alerts.domain.AlertRepository
import com.qlic.mobile.feature.alerts.domain.AlertState
import com.qlic.mobile.feature.alerts.domain.AuthorizedRecipient
import com.qlic.mobile.feature.alerts.domain.ConfigureThresholdInput
import com.qlic.mobile.feature.alerts.domain.ConsumptionThreshold
import com.qlic.mobile.feature.alerts.domain.LeakAlert
import com.qlic.mobile.feature.alerts.domain.NotificationPreference
import com.qlic.mobile.feature.alerts.domain.NotificationPreferenceInput
import com.qlic.mobile.feature.alerts.domain.RecipientInput
import com.qlic.mobile.feature.alerts.domain.Urgency
import com.qlic.mobile.feature.devices.domain.Device
import com.qlic.mobile.feature.devices.domain.DeviceRepository
import com.qlic.mobile.feature.devices.domain.DeviceStatus
import com.qlic.mobile.feature.devices.domain.RegisterDeviceInput
import com.qlic.mobile.feature.subscriptions.domain.PaymentRecord
import com.qlic.mobile.feature.subscriptions.domain.Plan
import com.qlic.mobile.feature.subscriptions.domain.PurchaseResult
import com.qlic.mobile.feature.subscriptions.domain.Subscription
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionRepository
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionStatus
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    @Test
    fun `complete aggregation exposes only real backend metrics`() = runTest {
        val viewModel = viewModel(
            devices = ApiResult.Success(listOf(device(DeviceStatus.Online), device(DeviceStatus.Offline))),
            alerts = ApiResult.Success(
                listOf(
                    alert(AlertState.Active, Urgency.Critical),
                    alert(AlertState.Acknowledged, Urgency.Moderate),
                ),
            ),
            subscription = ApiResult.Success(subscription()),
        )

        advanceUntilIdle()

        assertEquals(DeviceMetrics(total = 2, online = 1, attentionRequired = 1), viewModel.state.value.devices.data)
        assertEquals(AlertMetrics(open = 1, critical = 1, acknowledged = 1), viewModel.state.value.alerts.data)
        assertEquals(Plan.HomePro, viewModel.state.value.subscription.data?.currentPlan)
    }

    @Test
    fun `partial failure preserves successful sections`() = runTest {
        val viewModel = viewModel(
            devices = ApiResult.NetworkError,
            alerts = ApiResult.Success(listOf(alert(AlertState.Active, Urgency.Critical))),
            subscription = ApiResult.Success(subscription()),
        )

        advanceUntilIdle()

        assertEquals(DashboardError.Network, viewModel.state.value.devices.error)
        assertEquals(1, viewModel.state.value.alerts.data?.open)
        assertEquals(Plan.HomePro, viewModel.state.value.subscription.data?.currentPlan)
    }

    @Test
    fun `empty sources render zero counts and no invented subscription`() = runTest {
        val viewModel = viewModel(
            devices = ApiResult.Success(emptyList()),
            alerts = ApiResult.Success(emptyList()),
            subscription = ApiResult.Success(null),
        )

        advanceUntilIdle()

        assertEquals(DeviceMetrics(0, 0, 0), viewModel.state.value.devices.data)
        assertEquals(AlertMetrics(0, 0, 0), viewModel.state.value.alerts.data)
        assertNull(viewModel.state.value.subscription.data?.currentPlan)
        assertFalse(viewModel.state.value.subscription.isLoading)
    }

    @Test
    fun `fully failed aggregation exposes independent retryable errors`() = runTest {
        val viewModel = viewModel(
            devices = ApiResult.NetworkError,
            alerts = ApiResult.Failure(500),
            subscription = ApiResult.Unauthorized,
        )

        advanceUntilIdle()

        assertEquals(DashboardError.Network, viewModel.state.value.devices.error)
        assertEquals(DashboardError.Server, viewModel.state.value.alerts.error)
        assertEquals(DashboardError.Unauthorized, viewModel.state.value.subscription.error)
        assertTrue(viewModel.state.value.hasFailures)
    }

    private fun TestScope.viewModel(
        devices: ApiResult<List<Device>>,
        alerts: ApiResult<List<LeakAlert>>,
        subscription: ApiResult<Subscription?>,
    ) = DashboardViewModel(
        deviceRepository = FakeDeviceRepository(devices),
        alertRepository = FakeAlertRepository(alerts),
        subscriptionRepository = FakeSubscriptionRepository(subscription),
        dispatcher = StandardTestDispatcher(testScheduler),
    )

    private fun device(status: DeviceStatus) = Device(
        id = status.name,
        serialNumber = "QL-${status.name}",
        model = "Qlic Flow",
        accountId = "account-id",
        waterPointId = null,
        status = status,
        batteryPercentage = 80,
        lastSyncAt = "2026-11-12T08:00:00",
    )

    private fun alert(state: AlertState, urgency: Urgency) = LeakAlert(
        id = "$state-$urgency",
        accountId = "account-id",
        waterPointId = "water-point",
        state = state,
        urgency = urgency,
        detectedAt = null,
        estimatedVolumeLiters = BigDecimal.ONE,
        estimatedCostSoles = BigDecimal.ONE,
        recommendedAction = "Check",
        acknowledgedAction = null,
        previousAlertId = null,
        pushDispatched = true,
        notifiedRecipientsCount = 1,
    )

    private fun subscription() = Subscription(
        id = "subscription-id",
        accountId = "account-id",
        currentPlan = Plan.HomePro,
        nextBillingPlan = null,
        status = SubscriptionStatus.Active,
        trialUsed = true,
        paidFeaturesEnabled = true,
        maxWaterPointsAllowed = 6,
        startedAt = null,
        currentPeriodEndsAt = null,
        autoRenew = true,
    )

    private class FakeDeviceRepository(
        private val result: ApiResult<List<Device>>,
    ) : DeviceRepository {
        override suspend fun getDevices() = result
        override suspend fun registerDevice(input: RegisterDeviceInput) = ApiResult.Failure()
        override suspend fun assignWaterPoint(deviceId: String, waterPointId: String) = ApiResult.Failure()
    }

    private class FakeAlertRepository(
        private val result: ApiResult<List<LeakAlert>>,
    ) : AlertRepository {
        override suspend fun getAlerts() = result
        override suspend fun getAlert(alertId: String) = ApiResult.Failure()
        override suspend fun acknowledgeAlert(alertId: String, actionTaken: String) = ApiResult.Failure()
        override suspend fun getThresholds(): ApiResult<List<ConsumptionThreshold>> = ApiResult.Failure()
        override suspend fun configureThreshold(input: ConfigureThresholdInput) = ApiResult.Failure()
        override suspend fun getNotificationPreference(): ApiResult<NotificationPreference> = ApiResult.Failure()
        override suspend fun configureNotificationPreference(input: NotificationPreferenceInput) = ApiResult.Failure()
        override suspend fun getRecipients(): ApiResult<List<AuthorizedRecipient>> = ApiResult.Failure()
        override suspend fun addRecipient(input: RecipientInput) = ApiResult.Failure()
        override suspend fun toggleRecipient(recipientId: String, enabled: Boolean) = ApiResult.Failure()
    }

    private class FakeSubscriptionRepository(
        private val result: ApiResult<Subscription?>,
    ) : SubscriptionRepository {
        override suspend fun getCurrentSubscription() = result
        override suspend fun getPaymentHistory(): ApiResult<List<PaymentRecord>> = ApiResult.Failure()
        override suspend fun startTrial(): ApiResult<Subscription> = ApiResult.Failure()
        override suspend fun purchase(plan: Plan): ApiResult<PurchaseResult> = ApiResult.Failure()
        override suspend fun changePlan(subscriptionId: String, plan: Plan): ApiResult<Subscription> = ApiResult.Failure()
        override suspend fun cancel(subscriptionId: String): ApiResult<Subscription> = ApiResult.Failure()
    }
}
