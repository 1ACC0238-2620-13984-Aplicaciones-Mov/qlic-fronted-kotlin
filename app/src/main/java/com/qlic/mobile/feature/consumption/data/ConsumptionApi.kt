package com.qlic.mobile.feature.consumption.data

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.core.network.apiCall
import com.qlic.mobile.feature.consumption.domain.ActivityReport
import com.qlic.mobile.feature.consumption.domain.ActivitySchedule
import com.qlic.mobile.feature.consumption.domain.ConsumptionRepository
import com.qlic.mobile.feature.consumption.domain.WaterPointConsumption
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

data class ActivityScheduleDto(
    val openingTime: String,
    val closingTime: String,
    val activeDays: List<String>,
)

data class WaterPointConsumptionDto(
    val waterPointId: String,
    val totalLiters: Double,
    val withinActivityLiters: Double?,
    val outsideActivityLiters: Double?,
)

data class ActivityReportDto(
    val from: String,
    val to: String,
    val scheduleDefined: Boolean,
    val totalLiters: Double,
    val withinActivityLiters: Double?,
    val outsideActivityLiters: Double?,
    val outsideActivityPercentage: Double?,
    val byWaterPoint: List<WaterPointConsumptionDto>?,
)

interface ConsumptionApi {
    @GET("api/v1/consumption/activity-schedule")
    suspend fun getSchedule(): Response<ActivityScheduleDto>

    @PUT("api/v1/consumption/activity-schedule")
    suspend fun saveSchedule(@Body request: ActivityScheduleDto): Response<ActivityScheduleDto>

    @GET("api/v1/consumption/reports/activity-hours")
    suspend fun getReport(
        @Query("from") from: String,
        @Query("to") to: String,
    ): Response<ActivityReportDto>
}

class ConsumptionRepositoryImpl(private val api: ConsumptionApi) : ConsumptionRepository {
    override suspend fun getSchedule(): ApiResult<ActivitySchedule> =
        apiCall { api.getSchedule() }.mapResult { it.toDomain() }

    override suspend fun saveSchedule(schedule: ActivitySchedule): ApiResult<ActivitySchedule> =
        apiCall {
            api.saveSchedule(
                ActivityScheduleDto(
                    openingTime = schedule.openingTime,
                    closingTime = schedule.closingTime,
                    activeDays = schedule.activeDays.toList(),
                ),
            )
        }.mapResult { it.toDomain() }

    override suspend fun getReport(from: String, to: String): ApiResult<ActivityReport> =
        apiCall { api.getReport(from, to) }.mapResult { it.toDomain() }

    private fun ActivityScheduleDto.toDomain() = ActivitySchedule(
        openingTime = openingTime.take(5),
        closingTime = closingTime.take(5),
        activeDays = activeDays.toSet(),
    )

    private fun ActivityReportDto.toDomain() = ActivityReport(
        from = from,
        to = to,
        scheduleDefined = scheduleDefined,
        totalLiters = totalLiters,
        withinActivityLiters = withinActivityLiters,
        outsideActivityLiters = outsideActivityLiters,
        outsideActivityPercentage = outsideActivityPercentage,
        byWaterPoint = byWaterPoint.orEmpty().map {
            WaterPointConsumption(
                it.waterPointId,
                it.totalLiters,
                it.withinActivityLiters,
                it.outsideActivityLiters,
            )
        },
    )
}

internal inline fun <T, R> ApiResult<T>.mapResult(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(value))
    ApiResult.Empty -> ApiResult.Empty
    ApiResult.NetworkError -> ApiResult.NetworkError
    ApiResult.Unauthorized -> ApiResult.Unauthorized
    is ApiResult.Failure -> this
}
