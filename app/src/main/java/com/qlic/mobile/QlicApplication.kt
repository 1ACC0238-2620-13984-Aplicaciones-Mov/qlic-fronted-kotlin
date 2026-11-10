package com.qlic.mobile

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.qlic.mobile.core.network.ApiClientFactory
import com.qlic.mobile.core.localization.DataStoreLanguageRepository
import com.qlic.mobile.core.localization.LanguageRepository
import com.qlic.mobile.feature.auth.data.AndroidKeystoreTokenCipher
import com.qlic.mobile.feature.auth.data.AuthRepositoryImpl
import com.qlic.mobile.feature.auth.data.EncryptedSessionStore
import com.qlic.mobile.feature.auth.data.SessionStore
import com.qlic.mobile.feature.auth.domain.AuthRepository
import com.qlic.mobile.feature.auth.domain.SignInUseCase
import com.qlic.mobile.feature.auth.domain.SignUpUseCase

private val Context.languageDataStore by preferencesDataStore(name = "language_preferences")
private val Context.sessionDataStore by preferencesDataStore(name = "secure_session_preferences")

class QlicApplication : Application() {
    val languageRepository: LanguageRepository by lazy {
        DataStoreLanguageRepository(languageDataStore)
    }

    val sessionStore: SessionStore by lazy {
        EncryptedSessionStore(
            dataStore = sessionDataStore,
            tokenCipher = AndroidKeystoreTokenCipher(),
        )
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            api = ApiClientFactory.createAuthApi(),
            sessionStore = sessionStore,
        )
    }

    val signInUseCase: SignInUseCase by lazy { SignInUseCase(authRepository) }
    val signUpUseCase: SignUpUseCase by lazy { SignUpUseCase(authRepository) }
}
