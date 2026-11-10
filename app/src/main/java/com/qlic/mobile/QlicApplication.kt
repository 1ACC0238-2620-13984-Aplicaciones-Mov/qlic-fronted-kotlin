package com.qlic.mobile

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.qlic.mobile.core.localization.DataStoreLanguageRepository
import com.qlic.mobile.core.localization.LanguageRepository

private val Context.languageDataStore by preferencesDataStore(name = "language_preferences")

class QlicApplication : Application() {
    val languageRepository: LanguageRepository by lazy {
        DataStoreLanguageRepository(languageDataStore)
    }
}
