package com.tasbih.app

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tasbih.app.data.TasbihViewModel
import com.tasbih.app.ui.Looks
import com.tasbih.app.ui.TasbihApp
import com.tasbih.app.ui.TasbihTheme

class MainActivity : ComponentActivity() {
    private val vm: TasbihViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // keep the splash up until the saved look is loaded (so the app never flashes the wrong colours),
        // but never for more than 2.5 s, whatever happens
        val started = SystemClock.uptimeMillis()
        splash.setKeepOnScreenCondition { !vm.state.value.loaded && SystemClock.uptimeMillis() - started < 2500 }
        enableEdgeToEdge()
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            TasbihTheme(Looks[state.lookIndex.coerceIn(0, Looks.lastIndex)]) {
                TasbihApp(vm)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // the app may be closed by the system while in the background: save the count now
        vm.flush()
    }
}
