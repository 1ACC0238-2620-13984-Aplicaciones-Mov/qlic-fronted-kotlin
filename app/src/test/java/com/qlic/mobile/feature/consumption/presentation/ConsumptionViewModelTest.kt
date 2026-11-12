package com.qlic.mobile.feature.consumption.presentation

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.feature.consumption.domain.ActivityReport
import com.qlic.mobile.feature.consumption.domain.ActivitySchedule
import com.qlic.mobile.feature.consumption.domain.ConsumptionRepository
import com.qlic.mobile.feature.consumption.domain.isValidTime
import java.time.LocalDate
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
class ConsumptionViewModelTest {
    private val report = ActivityReport(
        from = "2026-11-06",
        to = "2026-11-12",
        scheduleDefined = true,
        totalLiters = 1250.0,
        withinActivityLiters = 1010.0,
        outsideActivityLiters = 240.0,
        outsideActivityPercentage = 19.2,
        byWaterPoint = emptyList(),
    )

    private fun TestScope.viewModel(repository: FakeConsumptionRepository) = ConsumptionViewModel(
        repository = repository,
        dispatcher = StandardTestDispatcher(testScheduler),
        today = { LocalDate.of(2026, 11, 12) },
    )

    @Test
    fun `time format accepts valid hours and rejects the rest`() {
        assertTrue(isValidTime("08:00"))
        assertTrue(isValidTime("23:59"))
        assertFalse(isValidTime("24:00"))
        assertFalse(isValidTime("8:00"))
        assertFalse(isValidTime("08:60"))
    }

    @Test
    fun `load requests the last seven days and exposes the report`() = runTest {
        val repository = FakeConsumptionRepository(report = ApiResult.Success(report))
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertEquals("2026-11-06" to "2026-11-12", repository.requestedPeriod)
        assertEquals(report, viewModel.state.value.report)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `a missing schedule is not an error`() = runTest {
        val repository = FakeConsumptionRepository(
            schedule = ApiResult.Failure(statusCode = 404),
            report = ApiResult.Success(report),
        )
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `an existing schedule fills the form`() = runTest {
        val repository = FakeConsumptionRepository(
            schedule = ApiResult.Success(ActivitySchedule("09:30", "21:00", setOf("MONDAY", "FRIDAY"))),
            report = ApiResult.Success(report),
        )
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertEquals("09:30", viewModel.state.value.openingTime)
        assertEquals("21:00", viewModel.state.value.closingTime)
        assertEquals(setOf("MONDAY", "FRIDAY"), viewModel.state.value.activeDays)
    }

    @Test
    fun `saving without active days is rejected and nothing is sent`() = runTest {
        val repository = FakeConsumptionRepository(report = ApiResult.Success(report))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.saveSchedule()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.invalidInput)
        assertNull(repository.savedSchedule)
    }

    @Test
    fun `saving a valid schedule sends it and refreshes the report`() = runTest {
        val repository = FakeConsumptionRepository(report = ApiResult.Success(report))
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        viewModel.onOpeningChanged("07:00")
        viewModel.onClosingChanged("22:00")
        viewModel.toggleDay("MONDAY")

        viewModel.saveSchedule()
        advanceUntilIdle()

        assertEquals(ActivitySchedule("07:00", "22:00", setOf("MONDAY")), repository.savedSchedule)
        assertTrue(viewModel.state.value.scheduleSaved)
        assertEquals(2, repository.reportCalls)
    }

    @Test
    fun `network failure while loading is reported`() = runTest {
        val repository = FakeConsumptionRepository(
            schedule = ApiResult.NetworkError,
            report = ApiResult.NetworkError,
        )
        val viewModel = viewModel(repository)

        advanceUntilIdle()

        assertEquals(ConsumptionError.Network, viewModel.state.value.error)
    }

    private class FakeConsumptionRepository(
        private val schedule: ApiResult<ActivitySchedule> = ApiResult.Failure(statusCode = 404),
        private val report: ApiResult<ActivityReport> = ApiResult.Empty,
    ) : ConsumptionRepository {
        var savedSchedule: ActivitySchedule? = null
        var requestedPeriod: Pair<String, String>? = null
        var reportCalls = 0

        override suspend fun getSchedule() = schedule

        override suspend fun saveSchedule(schedule: ActivitySchedule): ApiResult<ActivitySchedule> {
            savedSchedule = schedule
            return ApiResult.Success(schedule)
        }

        override suspend fun getReport(from: String, to: String): ApiResult<ActivityReport> {
            reportCalls++
            requestedPeriod = from to to
            return report
        }
    }
}
