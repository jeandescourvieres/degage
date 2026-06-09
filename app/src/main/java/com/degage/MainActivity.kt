package com.degage

import android.Manifest
import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.degage.prefs.AppPreferences
import com.degage.ui.DegageApp
import com.degage.ui.viewmodel.MainViewModel
import androidx.activity.viewModels
import com.degage.ui.theme.DegageTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val prefs by lazy { AppPreferences(this) }
    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Géré silencieusement — l'état se reflète dans l'UI */ }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* L'utilisateur a accepté ou refusé le rôle — on ne force pas */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRequiredPermissions()
        promptCallScreeningRole()
        viewModel.ensureBundledListLoaded()

        lifecycleScope.launch {
            val onboardingDone = prefs.onboardingDone.first()
            val welcomeShown = prefs.welcomeShown.first()
            setContent {
                DegageTheme {
                    DegageApp(
                        onboardingDone = onboardingDone,
                        welcomeShown = welcomeShown,
                        onOnboardingComplete = {
                            lifecycleScope.launch { prefs.setOnboardingDone() }
                        },
                        onWelcomeDismiss = {
                            lifecycleScope.launch { prefs.setWelcomeShown(true) }
                        }
                    )
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.ANSWER_PHONE_CALLS,
            )
        )
    }

    private fun promptCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java) ?: return
            if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                roleRequestLauncher.launch(intent)
            }
        }
    }
}
