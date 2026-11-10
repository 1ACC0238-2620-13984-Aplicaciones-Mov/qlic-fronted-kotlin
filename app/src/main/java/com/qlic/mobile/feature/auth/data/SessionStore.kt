package com.qlic.mobile.feature.auth.data

import com.qlic.mobile.feature.auth.domain.AuthSession
import kotlinx.coroutines.flow.Flow

interface SessionStore {
    val session: Flow<AuthSession?>

    suspend fun save(session: AuthSession)

    suspend fun clear()
}
