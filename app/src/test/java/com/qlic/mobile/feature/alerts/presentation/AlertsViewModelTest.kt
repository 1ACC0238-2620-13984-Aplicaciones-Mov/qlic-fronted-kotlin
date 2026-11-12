package com.qlic.mobile.feature.alerts.presentation

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
import com.qlic.mobile.feature.alerts.domain.ThresholdSource
import com.qlic.mobile.feature.alerts.domain.Urgency
import java.math.BigDecimal
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {
    @Test
    fun `load shows alerts and filters them by state and urgency`() = runTest {
        val repository = FakeAlertRepository().apply {
            alertsResult = ApiResult.Success(
                listOf(
                    alert("active-critical", AlertState.Active, Urgency.Critical),
                    alert("acknowledged", AlertState.Acknowledged, Urgency.Moderate),
                    alert("active-info", AlertState.Active, Urgency.Informational),
                ),
            )
        }
        val viewModel = AlertsViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.filteredAlerts.size)
        viewModel.selectFilter(AlertFilter.Open)
        assertEquals(listOf("active-critical", "active-info"), viewModel.state.value.filteredAlerts.map { it.id })
        viewModel.selectFilter(AlertFilter.Critical)
        assertEquals(listOf("active-critical"), viewModel.state.value.filteredAlerts.map { it.id })
    }

    @Test
    fun `empty response is represented without an error`() = runTest {
        val viewModel = AlertsViewModel(FakeAlertRepository(), StandardTestDispatcher(testScheduler))

        advanceUntilIdle()

        assertTrue(viewModel.state.value.alerts.isEmpty())
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `opening an alert loads its current detail`() = runTest {
        val repository = FakeAlertRepository().apply {
            alertResult = ApiResult.Success(alert("alert-detail", AlertState.Active, Urgency.Critical))
        }
        val viewModel = AlertsViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.openAlert("alert-detail")
        advanceUntilIdle()

        assertEquals("alert-detail", viewModel.state.value.selectedAlert?.id)
        assertFalse(viewModel.state.value.isDetailLoading)
    }

    @Test
    fun `acknowledging an alert updates list and detail without reloading all alerts`() = runTest {
        val active = alert("alert-id", AlertState.Active, Urgency.Critical)
        val acknowledged = active.copy(state = AlertState.Acknowledged, acknowledgedAction = "Valve closed")
        val repository = FakeAlertRepository().apply {
            alertsResult = ApiResult.Success(listOf(active))
            alertResult = ApiResult.Success(active)
            acknowledgeResult = ApiResult.Success(acknowledged)
        }
        val viewModel = AlertsViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()
        viewModel.openAlert("alert-id")
        advanceUntilIdle()

        viewModel.acknowledgeSelectedAlert("Valve closed")
        advanceUntilIdle()

        assertEquals(AlertState.Acknowledged, viewModel.state.value.selectedAlert?.state)
        assertEquals(AlertState.Acknowledged, viewModel.state.value.alerts.single().state)
        assertEquals(1, repository.getAlertsCalls)
    }

    @Test
    fun `blank acknowledgement is rejected before calling repository`() = runTest {
        val repository = FakeAlertRepository()
        val viewModel = AlertsViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.acknowledgeAlert("alert-id", "  ")
        advanceUntilIdle()

        assertEquals(AlertFormError.ActionRequired, viewModel.state.value.formError)
        assertEquals(0, repository.acknowledgeCalls)
    }

    @Test
    fun `settings load thresholds preferences and recipients`() = runTest {
        val repository = FakeAlertRepository().apply {
            thresholdsResult = ApiResult.Success(listOf(threshold()))
            preferenceResult = ApiResult.Success(preference())
            recipientsResult = ApiResult.Success(listOf(recipient()))
        }
        val viewModel = AlertsViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.loadSettings()
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.thresholds.size)
        assertEquals(15, viewModel.state.value.notificationPreference?.groupingWindowMinutes)
        assertEquals(1, viewModel.state.value.recipients.size)
        assertFalse(viewModel.state.value.isSettingsLoading)
    }

    @Test
    fun `successful settings mutations update only their affected data`() = runTest {
        val repository = FakeAlertRepository().apply {
            configureThresholdResult = ApiResult.Success(threshold())
            configurePreferenceResult = ApiResult.Success(preference())
            addRecipientResult = ApiResult.Success(recipient())
        }
        val viewModel = AlertsViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.saveThreshold("water-point", "HOURLY", "12.5", "10")
        advanceUntilIdle()
        viewModel.saveNotificationPreference("15", Urgency.Moderate)
        advanceUntilIdle()
        viewModel.addRecipient("Ana", "ana@example.com", "push-token", true)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.thresholds.size)
        assertEquals(15, viewModel.state.value.notificationPreference?.groupingWindowMinutes)
        assertEquals("ana@example.com", viewModel.state.value.recipients.single().email)
        assertEquals(0, repository.getThresholdCalls)
        assertEquals(0, repository.getPreferenceCalls)
        assertEquals(0, repository.getRecipientsCalls)
    }

    @Test
    fun `repeated write is ignored while acknowledgement is running`() = runTest {
        val repository = FakeAlertRepository().apply {
            acknowledgeGate = CompletableDeferred()
        }
        val viewModel = AlertsViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.acknowledgeAlert("alert-id", "Valve closed")
        viewModel.acknowledgeAlert("alert-id", "Second write")
        testScheduler.runCurrent()

        assertEquals(1, repository.acknowledgeCalls)
        assertTrue(viewModel.state.value.isSubmitting)

        repository.acknowledgeGate?.complete(
            ApiResult.Success(alert("alert-id", AlertState.Acknowledged, Urgency.Critical)),
        )
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `invalid recipient email is rejected locally`() = runTest {
        val repository = FakeAlertRepository()
        val viewModel = AlertsViewModel(repository, StandardTestDispatcher(testScheduler))
        advanceUntilIdle()

        viewModel.addRecipient("Ana", "invalid", "push-token", true)
        advanceUntilIdle()

        assertEquals(AlertFormError.InvalidRecipientEmail, viewModel.state.value.formError)
        assertEquals(0, repository.addRecipientCalls)
    }

    private fun alert(id: String, state: AlertState, urgency: Urgency) = LeakAlert(
        id = id,
        accountId = "account-id",
        waterPointId = "water-point",
        state = state,
        urgency = urgency,
        detectedAt = null,
        estimatedVolumeLiters = BigDecimal("4.2"),
        estimatedCostSoles = BigDecimal("8.4"),
        recommendedAction = "Check the valve",
        acknowledgedAction = null,
        previousAlertId = null,
        pushDispatched = true,
        notifiedRecipientsCount = 1,
    )

    private fun threshold() = ConsumptionThreshold(
        id = "threshold-id",
        waterPointId = "water-point",
        period = "HOURLY",
        customLimitLitersPerHour = BigDecimal("12.5"),
        baselineLitersPerHour = BigDecimal.TEN,
        effectiveLimitLitersPerHour = BigDecimal("12.5"),
        source = ThresholdSource.UserDefined,
        updatedAt = null,
    )

    private fun preference() = NotificationPreference(
        id = "preference-id",
        groupingWindowMinutes = 15,
        minimumUrgency = Urgency.Moderate,
        updatedAt = null,
    )

    private fun recipient() = AuthorizedRecipient(
        id = "recipient-id",
        fullName = "Ana",
        email = "ana@example.com",
        devicePushToken = "push-token",
        enabled = true,
        lastNotifiedAt = null,
    )

    private inner class FakeAlertRepository : AlertRepository {
        var alertsResult: ApiResult<List<LeakAlert>> = ApiResult.Success(emptyList())
        var alertResult: ApiResult<LeakAlert> = ApiResult.Success(alert("alert-id", AlertState.Active, Urgency.Critical))
        var acknowledgeResult: ApiResult<LeakAlert> = alertResult
        var acknowledgeGate: CompletableDeferred<ApiResult<LeakAlert>>? = null
        var thresholdsResult: ApiResult<List<ConsumptionThreshold>> = ApiResult.Success(emptyList())
        var configureThresholdResult: ApiResult<ConsumptionThreshold> = ApiResult.Success(threshold())
        var preferenceResult: ApiResult<NotificationPreference> = ApiResult.Success(preference())
        var configurePreferenceResult: ApiResult<NotificationPreference> = preferenceResult
        var recipientsResult: ApiResult<List<AuthorizedRecipient>> = ApiResult.Success(emptyList())
        var addRecipientResult: ApiResult<AuthorizedRecipient> = ApiResult.Success(recipient())

        var getAlertsCalls = 0
        var acknowledgeCalls = 0
        var getThresholdCalls = 0
        var getPreferenceCalls = 0
        var getRecipientsCalls = 0
        var addRecipientCalls = 0

        override suspend fun getAlerts(): ApiResult<List<LeakAlert>> {
            getAlertsCalls++
            return alertsResult
        }

        override suspend fun getAlert(alertId: String): ApiResult<LeakAlert> = alertResult

        override suspend fun acknowledgeAlert(alertId: String, actionTaken: String): ApiResult<LeakAlert> {
            acknowledgeCalls++
            return acknowledgeGate?.await() ?: acknowledgeResult
        }

        override suspend fun getThresholds(): ApiResult<List<ConsumptionThreshold>> {
            getThresholdCalls++
            return thresholdsResult
        }

        override suspend fun configureThreshold(input: ConfigureThresholdInput): ApiResult<ConsumptionThreshold> =
            configureThresholdResult

        override suspend fun getNotificationPreference(): ApiResult<NotificationPreference> {
            getPreferenceCalls++
            return preferenceResult
        }

        override suspend fun configureNotificationPreference(
            input: NotificationPreferenceInput,
        ): ApiResult<NotificationPreference> = configurePreferenceResult

        override suspend fun getRecipients(): ApiResult<List<AuthorizedRecipient>> {
            getRecipientsCalls++
            return recipientsResult
        }

        override suspend fun addRecipient(input: RecipientInput): ApiResult<AuthorizedRecipient> {
            addRecipientCalls++
            return addRecipientResult
        }

        override suspend fun toggleRecipient(
            recipientId: String,
            enabled: Boolean,
        ): ApiResult<AuthorizedRecipient> = ApiResult.Success(recipient().copy(enabled = enabled))
    }
}
