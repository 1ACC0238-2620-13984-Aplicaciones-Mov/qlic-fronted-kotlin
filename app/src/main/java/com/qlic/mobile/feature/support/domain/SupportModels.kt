package com.qlic.mobile.feature.support.domain

import com.qlic.mobile.core.network.ApiResult

enum class IncidentState { Pending, VisitRequested, InProgress, Resolved, Unknown }

data class Incident(
    val id: String,
    val description: String,
    val deviceId: String?,
    val state: IncidentState,
    val registeredAt: String,
    val resolution: String?,
)

data class RegisterIncidentInput(
    val description: String,
    val deviceId: String,
)

interface SupportRepository {
    suspend fun getIncidents(): ApiResult<List<Incident>>
    suspend fun registerIncident(input: RegisterIncidentInput): ApiResult<Incident>
}

const val MAX_INCIDENT_DESCRIPTION = 500
