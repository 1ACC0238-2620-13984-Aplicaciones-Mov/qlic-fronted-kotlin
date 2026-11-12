package com.qlic.mobile.feature.support.presentation

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.support.domain.Incident
import com.qlic.mobile.feature.support.domain.IncidentState
import com.qlic.mobile.feature.support.domain.RegisterIncidentInput
import com.qlic.mobile.feature.support.domain.SupportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupportViewModelTest {
    private fun incident(id: String, state: IncidentState = IncidentState.Pending) =
        Incident(id, "Sensor offline", "device-1", state, "2026-11-12T10:00:00Z", null)

    private fun TestScope.viewModel(repository: FakeSupportRepository) =
        SupportViewModel(repository, StandardTestDispatcher(testScheduler))

    @Test
    fun `load lists the incidents of the account`() = runTest {
        val repository = FakeSupportRepository(listOf(incident("a"), incident("b", IncidentState.Resolved)))
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertEquals(2, viewModel.state.value.incidents.size)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `an empty description is rejected`() = runTest {
        val repository = FakeSupportRepository()
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onDeviceSelected("device-1")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(SupportFormError.EmptyDescription, viewModel.state.value.formError)
        assertNull(repository.registered)
    }

    @Test
    fun `a description over 500 characters is rejected`() = runTest {
        val repository = FakeSupportRepository()
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onDeviceSelected("device-1")
        viewModel.onDescriptionChanged("x".repeat(501))

        viewModel.submit()

        assertEquals(SupportFormError.TooLong, viewModel.state.value.formError)
        assertNull(repository.registered)
    }

    @Test
    fun `an incident needs a device`() = runTest {
        val repository = FakeSupportRepository()
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onDescriptionChanged("No readings since yesterday")

        viewModel.submit()

        assertEquals(SupportFormError.NoDevice, viewModel.state.value.formError)
        assertNull(repository.registered)
    }

    @Test
    fun `a valid incident is registered and shown first`() = runTest {
        val repository = FakeSupportRepository(listOf(incident("old")))
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onDeviceSelected("device-1")
        viewModel.onDescriptionChanged("  No readings since yesterday  ")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(RegisterIncidentInput("No readings since yesterday", "device-1"), repository.registered)
        assertTrue(viewModel.state.value.registered)
        assertEquals("new", viewModel.state.value.incidents.first().id)
        assertEquals("", viewModel.state.value.description)
    }

    @Test
    fun `a server failure while registering is reported`() = runTest {
        val repository = FakeSupportRepository(registerResult = ApiResult.Failure(statusCode = 500))
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onDeviceSelected("device-1")
        viewModel.onDescriptionChanged("No readings")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(SupportError.Server, viewModel.state.value.error)
    }

    private inner class FakeSupportRepository(
        private val incidents: List<Incident> = emptyList(),
        private val registerResult: ApiResult<Incident> = ApiResult.Success(incident("new")),
    ) : SupportRepository {
        var registered: RegisterIncidentInput? = null

        override suspend fun getIncidents(): ApiResult<List<Incident>> = ApiResult.Success(incidents)

        override suspend fun registerIncident(input: RegisterIncidentInput): ApiResult<Incident> {
            registered = input
            return registerResult
        }
    }
}
