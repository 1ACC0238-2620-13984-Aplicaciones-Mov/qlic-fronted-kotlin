package com.qlic.mobile.feature.support.data

import com.qlic.mobile.core.network.ApiResult
import com.qlic.mobile.core.network.apiCall
import com.qlic.mobile.feature.support.domain.Incident
import com.qlic.mobile.feature.support.domain.IncidentState
import com.qlic.mobile.feature.support.domain.RegisterIncidentInput
import com.qlic.mobile.feature.support.domain.SupportRepository
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class RegisterIncidentDto(
    val description: String,
    val deviceId: String?,
    val waterPointId: String? = null,
    val alertId: String? = null,
)

data class IncidentDto(
    val id: String,
    val description: String,
    val deviceId: String?,
    val state: String,
    val registeredAt: String,
    val resolution: String?,
)

interface SupportApi {
    @GET("api/v1/incidents")
    suspend fun getIncidents(): Response<List<IncidentDto>>

    @POST("api/v1/incidents")
    suspend fun registerIncident(@Body request: RegisterIncidentDto): Response<IncidentDto>
}

class SupportRepositoryImpl(private val api: SupportApi) : SupportRepository {
    override suspend fun getIncidents(): ApiResult<List<Incident>> =
        apiCall { api.getIncidents() }.mapResult { list -> list.map { it.toDomain() } }

    override suspend fun registerIncident(input: RegisterIncidentInput): ApiResult<Incident> =
        apiCall {
            api.registerIncident(RegisterIncidentDto(description = input.description, deviceId = input.deviceId))
        }.mapResult { it.toDomain() }

    private fun IncidentDto.toDomain() = Incident(
        id = id,
        description = description,
        deviceId = deviceId,
        state = when (state) {
            "PENDING" -> IncidentState.Pending
            "VISIT_REQUESTED" -> IncidentState.VisitRequested
            "IN_PROGRESS" -> IncidentState.InProgress
            "RESOLVED" -> IncidentState.Resolved
            else -> IncidentState.Unknown
        },
        registeredAt = registeredAt,
        resolution = resolution,
    )
}

private inline fun <T, R> ApiResult<T>.mapResult(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(value))
    ApiResult.Empty -> ApiResult.Empty
    ApiResult.NetworkError -> ApiResult.NetworkError
    ApiResult.Unauthorized -> ApiResult.Unauthorized
    is ApiResult.Failure -> this
}
