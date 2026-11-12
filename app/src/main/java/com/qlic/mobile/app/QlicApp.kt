package com.qlic.mobile.app

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qlic.mobile.core.designsystem.QlicTheme
import com.qlic.mobile.core.localization.AppLanguage
import com.qlic.mobile.core.localization.LanguageRepository
import com.qlic.mobile.core.navigation.QlicNavHost
import com.qlic.mobile.feature.account.AccountUiState
import com.qlic.mobile.feature.alerts.presentation.AlertsViewModel
import com.qlic.mobile.feature.auth.presentation.AuthScreen
import com.qlic.mobile.feature.auth.presentation.AuthViewModel
import com.qlic.mobile.feature.dashboard.presentation.DashboardViewModel
import com.qlic.mobile.feature.devices.presentation.DevicesViewModel
import com.qlic.mobile.feature.subscriptions.presentation.SubscriptionViewModel
import kotlinx.coroutines.launch

@Composable
fun QlicRoot(
    languageRepository: LanguageRepository,
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    devicesViewModel: DevicesViewModel,
    alertsViewModel: AlertsViewModel,
    subscriptionViewModel: SubscriptionViewModel,
) {
    val selectedLanguage by languageRepository.language.collectAsStateWithLifecycle(initialValue = null)
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedLanguage) {
        selectedLanguage?.let { language ->
            val requestedLocales = LocaleListCompat.forLanguageTags(language.languageTag)
            if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != requestedLocales.toLanguageTags()) {
                AppCompatDelegate.setApplicationLocales(requestedLocales)
            }
        }
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            dashboardViewModel.retryDevices()
            dashboardViewModel.retryAlerts()
            dashboardViewModel.retrySubscription()
            devicesViewModel.retry()
            alertsViewModel.retry()
            subscriptionViewModel.retry()
        }
    }

    if (authState.isAuthenticated) {
        QlicApp(
            selectedLanguage = selectedLanguage ?: AppLanguage.English,
            onLanguageSelected = { language ->
                coroutineScope.launch { languageRepository.setLanguage(language) }
            },
            dashboardViewModel = dashboardViewModel,
            devicesViewModel = devicesViewModel,
            alertsViewModel = alertsViewModel,
            subscriptionViewModel = subscriptionViewModel,
            accountState = AccountUiState(
                fullName = authState.sessionFullName,
                email = authState.sessionEmail,
                accountId = authState.accountId,
            ),
            onSignOut = authViewModel::signOut,
        )
    } else {
        QlicTheme {
            AuthScreen(
                state = authState,
                onEmailChanged = authViewModel::onEmailChanged,
                onPasswordChanged = authViewModel::onPasswordChanged,
                onNameChanged = authViewModel::onNameChanged,
                onTermsChanged = authViewModel::onTermsChanged,
                onSubmit = authViewModel::submit,
                onModeChanged = authViewModel::onModeChanged,
                selectedLanguage = selectedLanguage ?: AppLanguage.English,
                onLanguageSelected = { language ->
                    coroutineScope.launch { languageRepository.setLanguage(language) }
                },
            )
        }
    }
}

@Composable
fun QlicApp(
    selectedLanguage: AppLanguage = AppLanguage.English,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    dashboardViewModel: DashboardViewModel? = null,
    devicesViewModel: DevicesViewModel? = null,
    alertsViewModel: AlertsViewModel? = null,
    subscriptionViewModel: SubscriptionViewModel? = null,
    accountState: AccountUiState = AccountUiState("", "", ""),
    onSignOut: () -> Unit = {},
) {
    QlicTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            QlicNavHost(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                dashboardViewModel = dashboardViewModel,
                devicesViewModel = devicesViewModel,
                alertsViewModel = alertsViewModel,
                subscriptionViewModel = subscriptionViewModel,
                accountState = accountState,
                onSignOut = onSignOut,
            )
        }
    }
}
