package com.qlic.mobile.core.localization

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

interface LanguageRepository {
    val language: Flow<AppLanguage>

    suspend fun setLanguage(language: AppLanguage)
}

class DataStoreLanguageRepository(
    private val dataStore: DataStore<Preferences>,
) : LanguageRepository {
    override val language: Flow<AppLanguage> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppLanguage.fromLanguageTag(preferences[languageKey])
        }
        .distinctUntilChanged()

    override suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[languageKey] = language.languageTag
        }
    }

    private companion object {
        val languageKey = stringPreferencesKey("app_language")
    }
}
