package com.qlic.mobile.core.network

import com.qlic.mobile.feature.auth.domain.AuthSession
import java.util.concurrent.atomic.AtomicReference

class SessionSnapshot {
    private val session = AtomicReference<AuthSession?>(null)
    private val languageTag = AtomicReference("en")

    fun currentSession(): AuthSession? = session.get()

    fun currentLanguageTag(): String = languageTag.get()

    fun updateSession(value: AuthSession?) {
        session.set(value)
    }

    fun updateLanguageTag(value: String) {
        languageTag.set(value.ifBlank { "en" })
    }
}
