package com.qlic.mobile.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.qlic.mobile.R

sealed class QlicDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int? = null,
) {
    data object Dashboard : QlicDestination("dashboard", R.string.nav_dashboard, R.drawable.ic_dashboard)
    data object Alerts : QlicDestination("alerts", R.string.nav_alerts, R.drawable.ic_alerts)
    data object Reports : QlicDestination("reports", R.string.nav_reports, R.drawable.ic_reports)
    data object Business : QlicDestination("business", R.string.nav_business, R.drawable.ic_business)
    data object More : QlicDestination("more", R.string.nav_more, R.drawable.ic_more)
    data object Devices : QlicDestination("devices", R.string.nav_devices)
    data object Tanks : QlicDestination("tanks", R.string.nav_tanks)
    data object Support : QlicDestination("support", R.string.nav_support)
    data object Account : QlicDestination("account", R.string.nav_account)

    companion object {
        val primary: List<QlicDestination> = listOf(Dashboard, Alerts, Reports, Business, More)
        val business: List<QlicDestination> = listOf(Devices, Tanks)
        val more: List<QlicDestination> = listOf(Support, Account)
    }
}
