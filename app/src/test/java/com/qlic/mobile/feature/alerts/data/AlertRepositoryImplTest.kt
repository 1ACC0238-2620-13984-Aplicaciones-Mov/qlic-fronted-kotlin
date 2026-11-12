package com.qlic.mobile.feature.alerts.data

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.core.network.SessionSnapshot
import com.qlic.mobile.feature.alerts.domain.AlertState
import com.qlic.mobile.feature.alerts.domain.ConfigureThresholdInput
import com.qlic.mobile.feature.alerts.domain.NotificationPreferenceInput
import com.qlic.mobile.feature.alerts.domain.RecipientInput
import com.qlic.mobile.feature.alerts.domain.Urgency
import com.qlic.mobile.feature.auth.domain.AuthSession
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AlertRepositoryImplTest {
    @Test
    fun `list uses session account and safely maps alert enums and local date`() = runBlocking {
        val api = FakeAlertApi().apply {
            alerts = listOf(alertDto(state = "FUTURE_STATE", urgency = "FUTURE_URGENCY"))
        }

        val result = AlertRepositoryImpl(api, authenticatedSnapshot()).getAlerts()

        assertEquals("account-id", api.requestedAccountId)
        val alert = (result as ApiResult.Success).value.single()
        assertEquals(AlertState.Unknown, alert.state)
        assertEquals(Urgency.Unknown, alert.urgency)
        assertEquals(LocalDateTime.of(2026, 11, 11, 20, 15, 30), alert.detectedAt)
    }

    @Test
    fun `detail includes both alert and session account identifiers`() = runBlocking {
        val api = FakeAlertApi()

        val result = AlertRepositoryImpl(api, authenticatedSnapshot()).getAlert("alert-id")

        assertTrue(result is ApiResult.Success)
        assertEquals("alert-id", api.requestedAlertId)
        assertEquals("account-id", api.requestedAccountId)
    }

    @Test
    fun `acknowledgement injects session account and action`() = runBlocking {
        val api = FakeAlertApi()

        val result = AlertRepositoryImpl(api, authenticatedSnapshot())
            .acknowledgeAlert("alert-id", "Closed the main valve")

        assertTrue(result is ApiResult.Success)
        assertEquals("alert-id", api.acknowledgedAlertId)
        assertEquals("account-id", api.acknowledgeRequest?.accountId)
        assertEquals("Closed the main valve", api.acknowledgeRequest?.actionTaken)
    }

    @Test
    fun `threshold operations preserve decimal values and account`() = runBlocking {
        val api = FakeAlertApi()
        val repository = AlertRepositoryImpl(api, authenticatedSnapshot())

        val listed = repository.getThresholds()
        val configured = repository.configureThreshold(
            ConfigureThresholdInput(
                waterPointId = "water-point-id",
                period = "HOURLY",
                customLimitLitersPerHour = BigDecimal("125.50"),
                baselineLitersPerHour = BigDecimal("100.00"),
            ),
        )

        assertTrue(listed is ApiResult.Success)
        assertTrue(configured is ApiResult.Success)
        assertEquals("account-id", api.thresholdRequest?.accountId)
        assertEquals(BigDecimal("125.50"), api.thresholdRequest?.customLimitLitersPerHour)
    }

    @Test
    fun `notification preference operations use supported urgency values`() = runBlocking {
        val api = FakeAlertApi()
        val repository = AlertRepositoryImpl(api, authenticatedSnapshot())

        val current = repository.getNotificationPreference()
        val configured = repository.configureNotificationPreference(
            NotificationPreferenceInput(45, Urgency.Critical),
        )

        assertTrue(current is ApiResult.Success)
        assertTrue(configured is ApiResult.Success)
        assertEquals("account-id", api.preferenceRequest?.accountId)
        assertEquals("CRITICAL", api.preferenceRequest?.minimumUrgency)
    }

    @Test
    fun `recipient operations map account and toggle values`() = runBlocking {
        val api = FakeAlertApi()
        val repository = AlertRepositoryImpl(api, authenticatedSnapshot())

        val listed = repository.getRecipients()
        val added = repository.addRecipient(
            RecipientInput("Ana Ruiz", "ana@qlic.com", "push-token", true),
        )
        val toggled = repository.toggleRecipient("recipient-id", false)

        assertTrue(listed is ApiResult.Success)
        assertTrue(added is ApiResult.Success)
        assertTrue(toggled is ApiResult.Success)
        assertEquals("account-id", api.recipientRequest?.accountId)
        assertEquals("recipient-id", api.toggledRecipientId)
        assertEquals(false, api.toggleRequest?.enabled)
    }

    @Test
    fun `missing session stops all account scoped requests`() = runBlocking {
        val api = FakeAlertApi()

        val result = AlertRepositoryImpl(api, SessionSnapshot()).getAlerts()

        assertEquals(ApiResult.Unauthorized, result)
        assertEquals(0, api.calls)
    }

    private fun authenticatedSnapshot() = SessionSnapshot().apply {
        updateSession(AuthSession("jwt", "account-id"))
    }

    private fun alertDto(
        state: String = "ACTIVE",
        urgency: String = "CRITICAL",
    ) = LeakAlertDto(
        id = "alert-id",
        accountId = "account-id",
        waterPointId = "water-point-id",
        state = state,
        urgency = urgency,
        detectedAt = "2026-11-11T20:15:30",
        estimatedVolumeLiters = BigDecimal("15.25"),
        estimatedCostSoles = BigDecimal("4.75"),
        recommendedAction = "Close the valve",
        acknowledgedAction = null,
        previousAlertId = null,
        pushDispatched = true,
        notifiedRecipientsCount = 1,
    )

    private fun thresholdDto() = ConsumptionThresholdDto(
        id = "threshold-id",
        accountId = "account-id",
        waterPointId = "water-point-id",
        period = "HOURLY",
        customLimitLitersPerHour = BigDecimal("125.50"),
        baselineLitersPerHour = BigDecimal("100.00"),
        effectiveLimitLitersPerHour = BigDecimal("125.50"),
        source = "USER_DEFINED",
        updatedAt = "2026-11-11T20:15:30",
    )

    private fun preferenceDto() = NotificationPreferenceDto(
        id = "preference-id",
        accountId = "account-id",
        groupingWindowMinutes = 30,
        minimumUrgency = "MODERATE",
        updatedAt = "2026-11-11T20:15:30",
    )

    private fun recipientDto(enabled: Boolean = true) = AuthorizedRecipientDto(
        id = "recipient-id",
        accountId = "account-id",
        fullName = "Ana Ruiz",
        email = "ana@qlic.com",
        devicePushToken = "push-token",
        enabled = enabled,
        lastNotifiedAt = null,
    )

    private inner class FakeAlertApi : AlertApi {
        var alerts = listOf(alertDto())
        var requestedAccountId: String? = null
        var requestedAlertId: String? = null
        var acknowledgedAlertId: String? = null
        var acknowledgeRequest: AcknowledgeAlertDto? = null
        var thresholdRequest: ConfigureThresholdDto? = null
        var preferenceRequest: ConfigureNotificationPreferenceDto? = null
        var recipientRequest: AddRecipientDto? = null
        var toggledRecipientId: String? = null
        var toggleRequest: ToggleRecipientDto? = null
        var calls = 0

        override suspend fun getAlerts(accountId: String): Response<List<LeakAlertDto>> {
            calls++
            requestedAccountId = accountId
            return Response.success(alerts)
        }

        override suspend fun getAlert(alertId: String, accountId: String): Response<LeakAlertDto> {
            calls++
            requestedAlertId = alertId
            requestedAccountId = accountId
            return Response.success(alertDto())
        }

        override suspend fun acknowledgeAlert(
            alertId: String,
            request: AcknowledgeAlertDto,
        ): Response<LeakAlertDto> {
            calls++
            acknowledgedAlertId = alertId
            acknowledgeRequest = request
            return Response.success(alertDto(state = "ACKNOWLEDGED"))
        }

        override suspend fun getThresholds(accountId: String): Response<List<ConsumptionThresholdDto>> {
            calls++
            requestedAccountId = accountId
            return Response.success(listOf(thresholdDto()))
        }

        override suspend fun configureThreshold(request: ConfigureThresholdDto): Response<ConsumptionThresholdDto> {
            calls++
            thresholdRequest = request
            return Response.success(thresholdDto())
        }

        override suspend fun getPreferences(accountId: String): Response<NotificationPreferenceDto> {
            calls++
            requestedAccountId = accountId
            return Response.success(preferenceDto())
        }

        override suspend fun configurePreferences(
            request: ConfigureNotificationPreferenceDto,
        ): Response<NotificationPreferenceDto> {
            calls++
            preferenceRequest = request
            return Response.success(preferenceDto())
        }

        override suspend fun getRecipients(accountId: String): Response<List<AuthorizedRecipientDto>> {
            calls++
            requestedAccountId = accountId
            return Response.success(listOf(recipientDto()))
        }

        override suspend fun addRecipient(request: AddRecipientDto): Response<AuthorizedRecipientDto> {
            calls++
            recipientRequest = request
            return Response.success(201, recipientDto())
        }

        override suspend fun toggleRecipient(
            recipientId: String,
            request: ToggleRecipientDto,
        ): Response<AuthorizedRecipientDto> {
            calls++
            toggledRecipientId = recipientId
            toggleRequest = request
            return Response.success(recipientDto(enabled = request.enabled))
        }
    }
}
