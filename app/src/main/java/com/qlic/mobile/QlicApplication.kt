package com.qlic.mobile

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.qlic.mobile.core.network.ApiClientFactory
import com.qlic.mobile.core.network.SessionSnapshot
import com.qlic.mobile.core.localization.DataStoreLanguageRepository
import com.qlic.mobile.core.localization.LanguageRepository
import com.qlic.mobile.feature.auth.data.AndroidKeystoreTokenCipher
import com.qlic.mobile.feature.auth.data.AuthRepositoryImpl
import com.qlic.mobile.feature.auth.data.EncryptedSessionStore
import com.qlic.mobile.feature.auth.data.SessionStore
import com.qlic.mobile.feature.auth.domain.AuthRepository
import com.qlic.mobile.feature.auth.domain.SignInUseCase
import com.qlic.mobile.feature.auth.domain.SignUpUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val Context.languageDataStore by preferencesDataStore(name = "language_preferences")
private val Context.sessionDataStore by preferencesDataStore(name = "secure_session_preferences")

class QlicApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val sessionSnapshot: SessionSnapshot by lazy { SessionSnapshot() }

    val languageRepository: LanguageRepository by lazy {
        DataStoreLanguageRepository(languageDataStore)
    }

    val sessionStore: SessionStore by lazy {
        EncryptedSessionStore(
            dataStore = sessionDataStore,
            tokenCipher = AndroidKeystoreTokenCipher(),
            onSessionChanged = sessionSnapshot::updateSession,
        )
    }

    val authenticatedRetrofit by lazy {
        ApiClientFactory.createAuthenticatedRetrofit(
            sessionSnapshot = sessionSnapshot,
            onUnauthorized = { sessionExpired.tryEmit(Unit) },
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

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            sessionStore.session.collectLatest(sessionSnapshot::updateSession)
        }
        applicationScope.launch {
            languageRepository.language.collectLatest { language ->
                sessionSnapshot.updateLanguageTag(language.languageTag)
            }
        }
        applicationScope.launch {
            sessionExpired.collectLatest { sessionStore.clear() }
        }
    }
}
