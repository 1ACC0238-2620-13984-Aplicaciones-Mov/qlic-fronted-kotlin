package com.qlic.mobile.core.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.qlic.mobile.R
import com.qlic.mobile.core.designsystem.QlicButton
import com.qlic.mobile.core.localization.AppLanguage

@Composable
fun QlicNavHost(
    isAuthenticated: Boolean,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    if (!isAuthenticated) {
        QlicSectionScreen(
            titleRes = R.string.auth_required_title,
            bodyRes = R.string.auth_required_body,
            testTag = "screen-auth-required",
        )
        return
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
                                    popUpTo(QlicDestination.Dashboard.route) {
                                        saveState = true
                                    }
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
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageSelected,
                )
            }
            composable(QlicDestination.Alerts.route) {
                QlicSectionScreen(R.string.alerts_title, R.string.alerts_body, "screen-alerts")
            }
            composable(QlicDestination.Reports.route) {
                QlicSectionScreen(R.string.reports_title, R.string.reports_body, "screen-reports")
            }
            composable(QlicDestination.Business.route) {
                BusinessScreen(
                    onDevicesClick = { navController.navigate(QlicDestination.Devices.route) },
                    onTanksClick = { navController.navigate(QlicDestination.Tanks.route) },
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
                QlicSectionScreen(R.string.devices_title, R.string.devices_body, "screen-devices")
            }
            composable(QlicDestination.Tanks.route) {
                QlicSectionScreen(R.string.tanks_title, R.string.tanks_body, "screen-tanks")
            }
            composable(QlicDestination.Support.route) {
                QlicSectionScreen(R.string.support_title, R.string.support_body, "screen-support")
            }
            composable(QlicDestination.Account.route) {
                QlicSectionScreen(R.string.account_title, R.string.account_body, "screen-account")
            }
        }
    }
}

@Composable
private fun DashboardScreen(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen-dashboard")
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.dashboard_eyebrow), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.dashboard_title), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.dashboard_summary), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.language_heading), style = MaterialTheme.typography.titleMedium)
        LanguageControls(selectedLanguage, onLanguageSelected)
    }
}

@Composable
private fun BusinessScreen(
    onDevicesClick: () -> Unit,
    onTanksClick: () -> Unit,
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
        QlicButton(stringResource(R.string.nav_account), onAccountClick)
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
private fun QlicSectionScreen(
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
    testTag: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(testTag)
            .padding(PaddingValues(24.dp)),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(titleRes), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(bodyRes), style = MaterialTheme.typography.bodyLarge)
    }
}
