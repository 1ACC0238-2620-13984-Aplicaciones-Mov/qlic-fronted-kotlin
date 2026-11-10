package com.qlic.mobile.feature.auth.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.qlic.mobile.feature.auth.domain.AuthSession
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EncryptedSessionStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var store: EncryptedSessionStore

    @Before
    fun setUp() {
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val file = File(temporaryFolder.newFolder("session"), "session.preferences_pb")
        store = EncryptedSessionStore(
            dataStore = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }),
            tokenCipher = PrefixTokenCipher(),
        )
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `session is encrypted and persisted`() = runBlocking {
        val session = AuthSession("jwt", "account-uuid", expiresAt = "2026-12-01T00:00:00Z")

        store.save(session)
        assertEquals(session, store.session.first())
    }

    @Test
    fun `clear keeps an empty store unauthenticated`() = runBlocking {
        store.clear()
        assertNull(store.session.first())
    }

    private class PrefixTokenCipher : TokenCipher {
        override fun encrypt(plainText: String): String = "encrypted:$plainText"
        override fun decrypt(cipherText: String): String = cipherText.removePrefix("encrypted:")
    }
}
