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
import com.qlic.mobile.feature.alerts.data.AlertApi
import com.qlic.mobile.feature.alerts.data.AlertRepositoryImpl
import com.qlic.mobile.feature.alerts.domain.AlertRepository
import com.qlic.mobile.feature.consumption.data.ConsumptionApi
import com.qlic.mobile.feature.consumption.data.ConsumptionRepositoryImpl
import com.qlic.mobile.feature.consumption.domain.ConsumptionRepository
import com.qlic.mobile.feature.devices.data.DeviceApi
import com.qlic.mobile.feature.sharing.data.SharingApi
import com.qlic.mobile.feature.sharing.data.SharingRepositoryImpl
import com.qlic.mobile.feature.sharing.domain.SharingRepository
import com.qlic.mobile.feature.support.data.SupportApi
import com.qlic.mobile.feature.support.data.SupportRepositoryImpl
import com.qlic.mobile.feature.support.domain.SupportRepository
import com.qlic.mobile.feature.devices.data.DeviceRepositoryImpl
import com.qlic.mobile.feature.devices.domain.DeviceRepository
import com.qlic.mobile.feature.subscriptions.data.SubscriptionApi
import com.qlic.mobile.feature.subscriptions.data.SubscriptionRepositoryImpl
import com.qlic.mobile.feature.subscriptions.domain.SubscriptionRepository
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

    val deviceRepository: DeviceRepository by lazy {
        DeviceRepositoryImpl(
            api = authenticatedRetrofit.create(DeviceApi::class.java),
            sessionSnapshot = sessionSnapshot,
        )
    }

    val alertRepository: AlertRepository by lazy {
        AlertRepositoryImpl(
            api = authenticatedRetrofit.create(AlertApi::class.java),
            sessionSnapshot = sessionSnapshot,
        )
    }

    val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepositoryImpl(
            api = authenticatedRetrofit.create(SubscriptionApi::class.java),
            sessionSnapshot = sessionSnapshot,
        )
    }

    val consumptionRepository: ConsumptionRepository by lazy {
        ConsumptionRepositoryImpl(authenticatedRetrofit.create(ConsumptionApi::class.java))
    }

    val supportRepository: SupportRepository by lazy {
        SupportRepositoryImpl(authenticatedRetrofit.create(SupportApi::class.java))
    }

    val sharingRepository: SharingRepository by lazy {
        SharingRepositoryImpl(authenticatedRetrofit.create(SharingApi::class.java))
    }

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
