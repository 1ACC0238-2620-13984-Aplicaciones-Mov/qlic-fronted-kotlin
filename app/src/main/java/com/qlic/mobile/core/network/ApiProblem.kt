package com.qlic.mobile.core.network

import com.google.gson.annotations.SerializedName

data class ApiProblem(
    @SerializedName("title") val title: String? = null,
    @SerializedName("detail") val detail: String? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("code") val code: String? = null,
)
