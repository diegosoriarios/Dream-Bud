package com.diego.dreambud

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.diego.dreambud.security.BiometricPromptResult
import com.diego.dreambud.ui.navigation.DreamBudNavHost
import com.diego.dreambud.ui.screens.LockScreen
import com.diego.dreambud.ui.theme.DreamBudTheme
import com.diego.dreambud.ui.viewmodel.DreamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity() {
    private val dreamViewModel: DreamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openDreamEditor = intent?.getBooleanExtra(EXTRA_OPEN_DREAM_EDITOR, false) == true
        val prefillTitle = if (openDreamEditor) {
            "Morning Dream • " + SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
        } else {
            null
        }

        setContent {
            DreamBudTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isBiometricLockEnabled by dreamViewModel.isBiometricLockEnabled.collectAsState()
                    val isAppUnlocked by dreamViewModel.isAppUnlocked.collectAsState()

                    if (isBiometricLockEnabled && !isAppUnlocked) {
                        LockScreen(
                            onUnlockRequested = { triggerBiometricPrompt() }
                        )
                    } else {
                        DreamBudNavHost(
                            viewModel = dreamViewModel,
                            startOnDreamEditor = openDreamEditor,
                            dreamEditorPrefillTitle = prefillTitle
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (dreamViewModel.isBiometricLockEnabled.value && !dreamViewModel.isAppUnlocked.value) {
            triggerBiometricPrompt()
        }
    }

    override fun onStop() {
        super.onStop()
        dreamViewModel.lockApp()
    }

    private fun triggerBiometricPrompt() {
        dreamViewModel.biometricPromptManager.showBiometricPrompt(
            activity = this,
            title = "Unlock Dream Journal",
            subtitle = "Verify identity to access your private dreams"
        ) { result ->
            if (result is BiometricPromptResult.Success) {
                dreamViewModel.setAppUnlocked(true)
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_DREAM_EDITOR = "extra_open_dream_editor"
    }
}
