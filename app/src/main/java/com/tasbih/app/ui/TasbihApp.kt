package com.tasbih.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.BlurCircular
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tasbih.app.data.TasbihViewModel
import kotlinx.serialization.Serializable

@Serializable object TodayRoute
@Serializable object ProgressRoute
@Serializable object SettingsRoute

private class Destination(val route: Any, val tab: NavTab)

private val Destinations = listOf(
    Destination(TodayRoute, NavTab("Tasbih", Icons.Rounded.BlurCircular)),
    Destination(ProgressRoute, NavTab("Progress", Icons.Rounded.BarChart)),
    Destination(SettingsRoute, NavTab("Settings", Icons.Rounded.Settings)),
)

@Composable
fun TasbihApp(vm: TasbihViewModel) {
    val s by vm.state.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()
    val look = Looks[s.lookIndex.coerceIn(0, Looks.lastIndex)]

    val context = LocalContext.current
    val feedback = remember { Feedback(context) }
    DisposableEffect(Unit) { onDispose { feedback.release() } }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(look.bgTop, look.bgBottom)))
    ) {
        Backdrop(look)
        if (!s.loaded || !history.ready) return@Box

        if (s.name.isBlank()) {
            Onboarding(look) { vm.setName(it) }
            return@Box
        }

        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val destination = entry?.destination

        // The highlighted tab changes the instant it is tapped; it does not wait for the screen change.
        var selected by rememberSaveable { mutableStateOf(0) }
        LaunchedEffect(destination) {
            val index = Destinations.indexOfFirst { destination?.hasRoute(it.route::class) == true }
            if (index >= 0) selected = index
        }

        // room the screens leave at the bottom for the floating bar
        val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val bottomPadding = navInset + 84.dp

        fun go(index: Int) {
            selected = index
            nav.navigate(Destinations[index].route) {
                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        // No fade between tabs: switching feels immediate.
        NavHost(
            navController = nav,
            startDestination = TodayRoute,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable<TodayRoute> {
                TodayScreen(
                    vm = vm,
                    s = s,
                    look = look,
                    today = today,
                    hint = history.allTime < 8L,
                    feedback = feedback,
                    bottomPadding = bottomPadding,
                    onOpenProgress = { go(1) },
                )
            }
            composable<ProgressRoute> { ProgressScreen(s, look, history, today, bottomPadding) }
            composable<SettingsRoute> { SettingsScreen(vm, s, look, feedback, bottomPadding) }
        }

        // a soft fade so scrolling content melts away behind the floating bar
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(navInset + 110.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, look.bgBottom.copy(alpha = 0.92f))))
        )

        FloatingNav(
            look = look,
            tabs = Destinations.map { it.tab },
            selectedIndex = selected,
            onSelect = { go(it) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
        )

        // first time on the home screen: a short walkthrough
        if (!s.tutorialDone) {
            TutorialOverlay(look, Destinations.map { it.tab }, onDone = { vm.setTutorialDone() })
        }
    }
}

/** First launch: a calm welcome that asks for the user's name. */
@Composable
private fun Onboarding(look: Look, onDone: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val submit: () -> Unit = { if (name.isNotBlank()) onDone(name) }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(32.dp))
        Text("بِسْمِ ٱللَّٰهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", fontSize = 26.sp, color = look.accent, textAlign = TextAlign.Center)
        Spacer(Modifier.height(36.dp))
        Text("Assalamu alaikum", fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontSize = 32.sp, color = look.onBg)
        Spacer(Modifier.height(6.dp))
        Text("Welcome to Tasbih", fontSize = 16.sp, color = look.muted)
        Spacer(Modifier.height(18.dp))
        HorizontalDivider(Modifier.width(56.dp), color = look.accent.copy(alpha = 0.5f))
        Spacer(Modifier.height(18.dp))
        Text(
            "A quiet place for your dhikr.\nWhat should I call you?",
            fontSize = 17.sp,
            lineHeight = 26.sp,
            color = look.onBg,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        NameField(look, name, { name = it }, onDone = { submit() }, modifier = Modifier.fillMaxWidth())
        AnimatedVisibility(visible = name.isNotBlank(), enter = fadeIn(tween(250)), exit = fadeOut(tween(150))) {
            Text(
                "Assalamu alaikum, ${name.trim()}",
                Modifier.padding(top = 16.dp),
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 20.sp,
                color = look.accent,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { submit() },
            enabled = name.isNotBlank(),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = look.accent,
                contentColor = look.onAccent,
                disabledContainerColor = look.line,
                disabledContentColor = look.muted,
            ),
        ) {
            Text("Continue", fontSize = 17.sp)
        }
        Spacer(Modifier.height(22.dp))
        Text(
            "Works fully offline. Your name and your dhikr stay on this phone.",
            fontSize = 12.sp,
            color = look.muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
    }
}
