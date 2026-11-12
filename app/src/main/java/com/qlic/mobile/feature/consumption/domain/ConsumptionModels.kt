package com.qlic.mobile.feature.consumption.domain

import com.qlic.mobile.core.network.ApiResult

data class ActivitySchedule(
    val openingTime: String,
    val closingTime: String,
    val activeDays: Set<String>,
)

data class WaterPointConsumption(
    val waterPointId: String,
    val totalLiters: Double,
    val withinActivityLiters: Double?,
    val outsideActivityLiters: Double?,
)

data class ActivityReport(
    val from: String,
    val to: String,
    val scheduleDefined: Boolean,
    val totalLiters: Double,
    val withinActivityLiters: Double?,
    val outsideActivityLiters: Double?,
    val outsideActivityPercentage: Double?,
    val byWaterPoint: List<WaterPointConsumption>,
)

interface ConsumptionRepository {
    suspend fun getSchedule(): ApiResult<ActivitySchedule>
    suspend fun saveSchedule(schedule: ActivitySchedule): ApiResult<ActivitySchedule>
    suspend fun getReport(from: String, to: String): ApiResult<ActivityReport>
}

val WEEK_DAYS: List<String> = listOf(
    "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY",
)

private val TIME_PATTERN = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

fun isValidTime(value: String): Boolean = TIME_PATTERN.matches(value.trim())
