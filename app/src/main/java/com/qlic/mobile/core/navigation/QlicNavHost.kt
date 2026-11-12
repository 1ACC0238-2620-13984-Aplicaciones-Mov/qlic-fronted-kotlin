package com.qlic.mobile.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.designsystem.UnavailableFeature
import com.qlic.mobile.core.localization.AppLanguage
import com.qlic.mobile.feature.account.AccountScreen
import com.qlic.mobile.feature.account.AccountUiState
import com.qlic.mobile.feature.alerts.presentation.AlertDetailScreen
import com.qlic.mobile.feature.alerts.presentation.AlertSettingsScreen
import com.qlic.mobile.feature.alerts.presentation.AlertsScreen
import com.qlic.mobile.feature.alerts.presentation.AlertsUiState
import com.qlic.mobile.feature.alerts.presentation.AlertsViewModel
import com.qlic.mobile.feature.dashboard.presentation.DashboardError
import com.qlic.mobile.feature.dashboard.presentation.DashboardScreen
import com.qlic.mobile.feature.dashboard.presentation.DashboardSection
import com.qlic.mobile.feature.dashboard.presentation.DashboardUiState
import com.qlic.mobile.feature.dashboard.presentation.DashboardViewModel
import com.qlic.mobile.feature.devices.presentation.DeviceRegistrationScreen
import com.qlic.mobile.feature.devices.presentation.DevicesScreen
import com.qlic.mobile.feature.devices.presentation.DevicesUiState
import com.qlic.mobile.feature.devices.presentation.DevicesViewModel
import com.qlic.mobile.feature.subscriptions.presentation.SubscriptionScreen
import com.qlic.mobile.feature.subscriptions.presentation.SubscriptionUiState
import com.qlic.mobile.feature.subscriptions.presentation.SubscriptionViewModel

@Composable
fun QlicNavHost(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    dashboardViewModel: DashboardViewModel?,
    devicesViewModel: DevicesViewModel?,
    alertsViewModel: AlertsViewModel?,
    subscriptionViewModel: SubscriptionViewModel?,
    accountState: AccountUiState,
    onSignOut: () -> Unit,
) {
    val dashboardState = if (dashboardViewModel != null) {
        val state by dashboardViewModel.state.collectAsStateWithLifecycle()
        state
    } else {
        DashboardUiState(
            devices = DashboardSection(isLoading = false, error = DashboardError.Server),
            alerts = DashboardSection(isLoading = false, error = DashboardError.Server),
            subscription = DashboardSection(isLoading = false, error = DashboardError.Server),
        )
    }
    val devicesState = if (devicesViewModel != null) {
        val state by devicesViewModel.state.collectAsStateWithLifecycle()
        state
    } else {
        DevicesUiState()
    }
    val alertsState = if (alertsViewModel != null) {
        val state by alertsViewModel.state.collectAsStateWithLifecycle()
        state
    } else {
        AlertsUiState()
    }
    val subscriptionState = if (subscriptionViewModel != null) {
        val state by subscriptionViewModel.state.collectAsStateWithLifecycle()
        state
    } else {
        SubscriptionUiState()
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                QlicDestination.primary.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(
                                destination.route,
                                navOptions {
                                    popUpTo(QlicDestination.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                },
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(requireNotNull(destination.iconRes)),
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) },
                        modifier = Modifier.testTag("bottom-nav-${destination.route}"),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = QlicDestination.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(QlicDestination.Dashboard.route) {
                DashboardScreen(
                    state = dashboardState,
                    onRetryDevices = { dashboardViewModel?.retryDevices() },
                    onRetryAlerts = { dashboardViewModel?.retryAlerts() },
                    onRetrySubscription = { dashboardViewModel?.retrySubscription() },
                    modifier = Modifier.testTag("screen-dashboard"),
                )
            }
            composable(QlicDestination.Alerts.route) {
                AlertsScreen(
                    state = alertsState,
                    onRetry = { alertsViewModel?.retry() },
                    onFilterSelected = { alertsViewModel?.selectFilter(it) },
                    onAlertClick = { alertId ->
                        alertsViewModel?.openAlert(alertId)
                        navController.navigate(QlicDestination.AlertDetail.createRoute(alertId))
                    },
                    onSettingsClick = {
                        alertsViewModel?.loadSettings()
                        navController.navigate(QlicDestination.AlertSettings.route)
                    },
                    modifier = Modifier.testTag("screen-alerts"),
                )
            }
            composable(QlicDestination.Reports.route) {
                UnavailableScreen(
                    R.string.reports_unavailable_title,
                    R.string.reports_unavailable_body,
                    "screen-reports",
                )
            }
            composable(QlicDestination.Business.route) {
                BusinessScreen(
                    onDevicesClick = { navController.navigate(QlicDestination.Devices.route) },
                    onTanksClick = { navController.navigate(QlicDestination.Tanks.route) },
                    onSubscriptionClick = { navController.navigate(QlicDestination.Subscription.route) },
                )
            }
            composable(QlicDestination.More.route) {
                MoreScreen(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageSelected,
                    onSupportClick = { navController.navigate(QlicDestination.Support.route) },
                    onAccountClick = { navController.navigate(QlicDestination.Account.route) },
                )
            }
            composable(QlicDestination.Devices.route) {
                DevicesScreen(
                    state = devicesState,
                    onRetry = { devicesViewModel?.retry() },
                    onRegisterClick = { navController.navigate(QlicDestination.DeviceRegistration.route) },
                    onAssignClick = { deviceId, waterPointId ->
                        devicesViewModel?.assignWaterPoint(deviceId, waterPointId)
                    },
                    modifier = Modifier.testTag("screen-devices"),
                )
            }
            composable(QlicDestination.DeviceRegistration.route) {
                DeviceRegistrationScreen(
                    state = devicesState,
                    onSubmit = { qr, serial, model -> devicesViewModel?.registerDevice(qr, serial, model) },
                    modifier = Modifier.testTag("screen-device-registration"),
                )
            }
            composable(QlicDestination.Tanks.route) {
                UnavailableScreen(
                    R.string.tanks_unavailable_title,
                    R.string.tanks_unavailable_body,
                    "screen-tanks",
                )
            }
            composable(QlicDestination.Support.route) {
                UnavailableScreen(
                    R.string.support_unavailable_title,
                    R.string.support_unavailable_body,
                    "screen-support",
                )
            }
            composable(QlicDestination.Account.route) {
                AccountScreen(accountState, onSignOut)
            }
            composable(
                route = QlicDestination.AlertDetail.route,
                arguments = listOf(navArgument("alertId") { type = NavType.StringType }),
            ) {
                AlertDetailScreen(
                    state = alertsState,
                    onBack = navController::navigateUp,
                    onAcknowledge = { alertsViewModel?.acknowledgeSelectedAlert(it) },
                    modifier = Modifier.testTag("screen-alert-detail"),
                )
            }
            composable(QlicDestination.AlertSettings.route) {
                AlertSettingsScreen(
                    state = alertsState,
                    onBack = navController::navigateUp,
                    onSaveThreshold = { waterPoint, period, limit, baseline ->
                        alertsViewModel?.saveThreshold(waterPoint, period, limit, baseline)
                    },
                    onSavePreference = { window, urgency ->
                        alertsViewModel?.saveNotificationPreference(window, urgency)
                    },
                    onAddRecipient = { name, email, token, enabled ->
                        alertsViewModel?.addRecipient(name, email, token, enabled)
                    },
                    onToggleRecipient = { id, enabled -> alertsViewModel?.toggleRecipient(id, enabled) },
                    modifier = Modifier.testTag("screen-alert-settings"),
                )
            }
            composable(QlicDestination.Subscription.route) {
                SubscriptionScreen(
                    state = subscriptionState,
                    onRetry = { subscriptionViewModel?.retry() },
                    onStartTrial = { subscriptionViewModel?.startTrial() },
                    onPurchase = { subscriptionViewModel?.purchase(it) },
                    onChangePlan = { subscriptionViewModel?.changePlan(it) },
                    onCancel = { subscriptionViewModel?.cancel() },
                    modifier = Modifier.testTag("screen-subscription"),
                )
            }
        }
    }
}

@Composable
private fun BusinessScreen(
    onDevicesClick: () -> Unit,
    onTanksClick: () -> Unit,
    onSubscriptionClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen-business")
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.business_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.business_body), style = MaterialTheme.typography.bodyLarge)
        QlicButton(
            text = stringResource(R.string.nav_devices),
            onClick = onDevicesClick,
            modifier = Modifier.testTag("business-devices"),
        )
        QlicButton(
            text = stringResource(R.string.nav_tanks),
            onClick = onTanksClick,
            modifier = Modifier.testTag("business-tanks"),
        )
        QlicButton(
            text = stringResource(R.string.subscription_title),
            onClick = onSubscriptionClick,
            modifier = Modifier.testTag("business-subscription"),
        )
    }
}

@Composable
private fun MoreScreen(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSupportClick: () -> Unit,
    onAccountClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen-more")
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.more_title), style = MaterialTheme.typography.headlineMedium)
        QlicButton(stringResource(R.string.nav_support), onSupportClick)
        QlicButton(
            stringResource(R.string.nav_account),
            onAccountClick,
            modifier = Modifier.testTag("more-account"),
        )
        Text(stringResource(R.string.language_heading), style = MaterialTheme.typography.titleMedium)
        LanguageControls(selectedLanguage, onLanguageSelected)
    }
}

@Composable
private fun LanguageControls(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    QlicButton(
        text = stringResource(R.string.language_english),
        onClick = { onLanguageSelected(AppLanguage.English) },
    )
    QlicButton(
        text = stringResource(R.string.language_spanish),
        onClick = { onLanguageSelected(AppLanguage.SpanishLatinAmerica) },
    )
    Text(
        text = stringResource(
            if (selectedLanguage == AppLanguage.English) {
                R.string.language_selected_english
            } else {
                R.string.language_selected_spanish
            },
        ),
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun UnavailableScreen(title: Int, description: Int, tag: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(tag)
            .padding(24.dp),
    ) {
        UnavailableFeature(
            title = stringResource(title),
            description = stringResource(description),
        )
    }
}
