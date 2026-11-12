package com.qlic.mobile.core.network

import com.google.gson.Gson
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.Response

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data object Empty : ApiResult<Nothing>
    data object Unauthorized : ApiResult<Nothing>
    data object NetworkError : ApiResult<Nothing>
    data class Failure(
        val statusCode: Int? = null,
        val problem: ApiProblem? = null,
    ) : ApiResult<Nothing>
}

suspend fun <T> apiCall(
    gson: Gson = Gson(),
    request: suspend () -> Response<T>,
): ApiResult<T> = try {
    val response = request()
    when {
        response.code() == 401 -> ApiResult.Unauthorized
        response.isSuccessful -> response.body()?.let(ApiResult<T>::Success) ?: ApiResult.Empty
        else -> ApiResult.Failure(
            statusCode = response.code(),
            problem = response.errorBody()?.charStream()?.use { reader ->
                runCatching { gson.fromJson(reader, ApiProblem::class.java) }.getOrNull()
            },
        )
    }
} catch (exception: CancellationException) {
    throw exception
} catch (_: IOException) {
    ApiResult.NetworkError
} catch (_: Exception) {
    ApiResult.Failure()
}
