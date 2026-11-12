package com.qlic.mobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qlic.mobile.app.QlicRoot
import com.qlic.mobile.feature.alerts.presentation.AlertsViewModel
import com.qlic.mobile.feature.auth.presentation.AuthViewModel
import com.qlic.mobile.feature.dashboard.presentation.DashboardViewModel
import com.qlic.mobile.feature.devices.presentation.DevicesViewModel
import com.qlic.mobile.feature.subscriptions.presentation.SubscriptionViewModel

class MainActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        val app = application as QlicApplication
        AuthViewModel.Factory(app.signInUseCase, app.signUpUseCase, app.sessionStore)
    }

    private val devicesViewModel: DevicesViewModel by viewModels {
        val app = application as QlicApplication
        QlicViewModelFactory { DevicesViewModel(app.deviceRepository) }
    }

    private val alertsViewModel: AlertsViewModel by viewModels {
        val app = application as QlicApplication
        QlicViewModelFactory { AlertsViewModel(app.alertRepository) }
    }

    private val subscriptionViewModel: SubscriptionViewModel by viewModels {
        val app = application as QlicApplication
        QlicViewModelFactory { SubscriptionViewModel(app.subscriptionRepository) }
    }

    private val dashboardViewModel: DashboardViewModel by viewModels {
        val app = application as QlicApplication
        QlicViewModelFactory {
            DashboardViewModel(
                deviceRepository = app.deviceRepository,
                alertRepository = app.alertRepository,
                subscriptionRepository = app.subscriptionRepository,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QlicRoot(
                languageRepository = (application as QlicApplication).languageRepository,
                authViewModel = authViewModel,
                dashboardViewModel = dashboardViewModel,
                devicesViewModel = devicesViewModel,
                alertsViewModel = alertsViewModel,
                subscriptionViewModel = subscriptionViewModel,
            )
        }
    }
}

private class QlicViewModelFactory<T : ViewModel>(
    private val create: () -> T,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <M : ViewModel> create(modelClass: Class<M>): M = create() as M
}
