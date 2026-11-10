package com.qlic.mobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.qlic.mobile.app.QlicRoot
import com.qlic.mobile.feature.auth.presentation.AuthViewModel

class MainActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        val app = application as QlicApplication
        AuthViewModel.Factory(app.signInUseCase, app.signUpUseCase, app.sessionStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QlicRoot(
                languageRepository = (application as QlicApplication).languageRepository,
                authViewModel = authViewModel,
            )
        }
    }
}
