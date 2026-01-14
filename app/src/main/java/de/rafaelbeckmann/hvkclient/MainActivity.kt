package de.rafaelbeckmann.hvkclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import de.rafaelbeckmann.hvkclient.domain.repository.SettingsRepository
import de.rafaelbeckmann.hvkclient.ui.main.MainScreen
import de.rafaelbeckmann.hvkclient.ui.theme.HvKClientTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var prefUtils: PrefUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show splash screen on all Android versions
        installSplashScreen() // only useful when customizing the splash screen
        enableEdgeToEdge()

        setContent {
            window.isNavigationBarContrastEnforced = false

            val useDynamicColor by settingsRepository
                .useDynamicColorFlow()
                .collectAsStateWithLifecycle(initialValue = true)

            HvKClientTheme(dynamicColor = useDynamicColor) {

                // TODO: inject with Hilt
                MainScreen(
                    settingsRepository = settingsRepository,
                )
            }
        }
    }
}