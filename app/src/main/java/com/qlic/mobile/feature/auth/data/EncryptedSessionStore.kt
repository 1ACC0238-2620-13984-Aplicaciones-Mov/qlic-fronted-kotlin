package com.qlic.mobile.feature.auth.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.qlic.mobile.feature.auth.domain.AuthSession
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class EncryptedSessionStore(
    private val dataStore: DataStore<Preferences>,
    private val tokenCipher: TokenCipher,
    private val nowProvider: () -> Instant = Instant::now,
) : SessionStore {
    override val session: Flow<AuthSession?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val encryptedToken = preferences[tokenKey] ?: return@map null
            val accountId = preferences[accountIdKey] ?: return@map null
            runCatching {
                val expiresAt = preferences[expiresAtKey]
                if (expiresAt != null && !Instant.parse(expiresAt).isAfter(nowProvider())) {
                    return@runCatching null
                }
                AuthSession(
                    accessToken = tokenCipher.decrypt(encryptedToken),
                    accountId = accountId,
                    email = preferences[emailKey].orEmpty(),
                    fullName = preferences[fullNameKey].orEmpty(),
                    tokenType = preferences[tokenTypeKey] ?: "Bearer",
                    expiresAt = expiresAt,
                )
            }.getOrNull()
        }

    override suspend fun save(session: AuthSession) {
        val encryptedToken = tokenCipher.encrypt(session.accessToken)
        dataStore.edit { preferences ->
            preferences[tokenKey] = encryptedToken
            preferences[accountIdKey] = session.accountId
            preferences[emailKey] = session.email
            preferences[fullNameKey] = session.fullName
            preferences[tokenTypeKey] = session.tokenType
            session.expiresAt?.let { preferences[expiresAtKey] = it }
                ?: preferences.remove(expiresAtKey)
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(tokenKey)
            preferences.remove(accountIdKey)
            preferences.remove(emailKey)
            preferences.remove(fullNameKey)
            preferences.remove(tokenTypeKey)
            preferences.remove(expiresAtKey)
        }
    }

    private companion object {
        val tokenKey = stringPreferencesKey("encrypted_access_token")
        val accountIdKey = stringPreferencesKey("account_id")
        val emailKey = stringPreferencesKey("email")
        val fullNameKey = stringPreferencesKey("full_name")
        val tokenTypeKey = stringPreferencesKey("token_type")
        val expiresAtKey = stringPreferencesKey("expires_at")
    }
}
