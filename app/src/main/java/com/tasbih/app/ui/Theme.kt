package com.tasbih.app.ui

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

class BeadPaint(val base: Color, val light: Color, val dark: Color)

/** A complete look: colours, bead materials and tassel. Chosen in Settings. */
class Look(
    val name: String,
    val dark: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val surface: Color,
    val card: Color,
    val accent: Color,
    val onAccent: Color,
    val onBg: Color,
    val muted: Color,
    val line: Color,
    val beads: List<BeadPaint>,
    /** Order of bead materials along the string (indexes into [beads]). */
    val pattern: List<Int>,
    val marker: BeadPaint,
    val ring: Color,
    val string: Color,
    val tassel: List<Color>,
    val band: List<Color>,
)

val Looks: List<Look> = listOf(
    Look(
        name = "Emerald Ivory",
        dark = false,
        bgTop = Color(0xFFF8F3E6), bgBottom = Color(0xFFECE3CC),
        surface = Color(0xFFFFFBF1), card = Color(0xFFEFE6D0),
        accent = Color(0xFF0F6B54), onAccent = Color(0xFFFFFFFF),
        onBg = Color(0xFF1B2B26), muted = Color(0xFF61736B), line = Color(0xFFD8CDB1),
        beads = listOf(
            BeadPaint(Color(0xFF1F8A6B), Color(0xFF8FE0C0), Color(0xFF0A4A38)), // emerald
            BeadPaint(Color(0xFF146B53), Color(0xFF4FB596), Color(0xFF063A2C)), // deep emerald
            BeadPaint(Color(0xFFF1EADB), Color(0xFFFFFFFF), Color(0xFFB2A890)), // pearl
            BeadPaint(Color(0xFFD9B25F), Color(0xFFFFE8A8), Color(0xFF8A6A22)), // gold
        ),
        pattern = listOf(0, 2, 0, 1, 0, 2, 0, 1, 0, 3),
        marker = BeadPaint(Color(0xFF0D4F3F), Color(0xFF3FA98B), Color(0xFF03221A)),
        ring = Color(0xFFC9A24D),
        string = Color(0xFF7F8B72),
        tassel = listOf(Color(0xFF3F9F83), Color(0xFF1E7A62), Color(0xFF0F4C3C)),
        band = listOf(Color(0xFFE2C27A), Color(0xFF9C7A30)),
    ),
    Look(
        name = "Rose Pearl",
        dark = false,
        bgTop = Color(0xFFFFF6F2), bgBottom = Color(0xFFF6D9DC),
        surface = Color(0xFFFFF9F6), card = Color(0xFFF8E4E4),
        accent = Color(0xFF9A3B5B), onAccent = Color(0xFFFFFFFF),
        onBg = Color(0xFF3A1F2A), muted = Color(0xFF8A6672), line = Color(0xFFE6C3C8),
        beads = listOf(
            BeadPaint(Color(0xFFF1E6DF), Color(0xFFFFFFFF), Color(0xFFB79F97)), // pearl
            BeadPaint(Color(0xFFE7A9B8), Color(0xFFFFD9E2), Color(0xFFA25B72)), // rose quartz
            BeadPaint(Color(0xFFCC907C), Color(0xFFF5CDB9), Color(0xFF7C493B)), // rose gold
            BeadPaint(Color(0xFFE9D3B0), Color(0xFFFFF1D6), Color(0xFFA98C5E)), // champagne
        ),
        pattern = listOf(0, 0, 1, 0, 0, 2, 0, 0, 1, 0, 0, 3),
        marker = BeadPaint(Color(0xFF8C2F52), Color(0xFFDE86A3), Color(0xFF3A0F20)),
        ring = Color(0xFFD9AE6B),
        string = Color(0xFFB58B7A),
        tassel = listOf(Color(0xFFE5B3A2), Color(0xFFC0806F), Color(0xFF8E5248)),
        band = listOf(Color(0xFFE6C58A), Color(0xFFA9803F)),
    ),
    Look(
        name = "Midnight Jade",
        dark = true,
        bgTop = Color(0xFF0B2227), bgBottom = Color(0xFF04100F),
        surface = Color(0xFF102A2D), card = Color(0xFF17393C),
        accent = Color(0xFFEAC980), onAccent = Color(0xFF2E2204),
        onBg = Color(0xFFF2EBD9), muted = Color(0xFFA9BDB7), line = Color(0xFF2B4C4E),
        beads = listOf(
            BeadPaint(Color(0xFF2E9477), Color(0xFF94E3C4), Color(0xFF0C4636)), // jade
            BeadPaint(Color(0xFF1D6E5B), Color(0xFF5FB89C), Color(0xFF08342A)), // deep jade
            BeadPaint(Color(0xFFEEE7D8), Color(0xFFFFFFFF), Color(0xFFA89F8B)), // pearl
            BeadPaint(Color(0xFFD9B25F), Color(0xFFFFE8A8), Color(0xFF8A6A22)), // gold
        ),
        pattern = listOf(0, 1, 0, 1, 2, 1, 0, 1, 0, 3),
        marker = BeadPaint(Color(0xFF0F5C4A), Color(0xFF5CC9A7), Color(0xFF04231C)),
        ring = Color(0xFFE6C27A),
        string = Color(0xFF7A8F86),
        tassel = listOf(Color(0xFF2F8F7A), Color(0xFF1D6A5A), Color(0xFF0F3F36)),
        band = listOf(Color(0xFFE3C07A), Color(0xFF9A7A32)),
    ),
    Look(
        name = "Sandalwood",
        dark = false,
        bgTop = Color(0xFFFBF3E3), bgBottom = Color(0xFFEBD9B8),
        surface = Color(0xFFFFF9EC), card = Color(0xFFF1E4CC),
        accent = Color(0xFF8A5A14), onAccent = Color(0xFFFFFFFF),
        onBg = Color(0xFF2A1D10), muted = Color(0xFF6F5E48), line = Color(0xFFDCC9A5),
        beads = listOf(
            BeadPaint(Color(0xFFB8651F), Color(0xFFF3B06B), Color(0xFF6B3208)), // amber
            BeadPaint(Color(0xFF5A2E12), Color(0xFFA6683D), Color(0xFF2A1305)), // chestnut
            BeadPaint(Color(0xFF2E2E36), Color(0xFF7B7B8A), Color(0xFF101015)), // ebony
        ),
        pattern = listOf(0, 1, 2, 1, 0, 2, 1),
        marker = BeadPaint(Color(0xFF6A2F3A), Color(0xFFC8808C), Color(0xFF2E1015)),
        ring = Color(0xFFE6C27A),
        string = Color(0xFF8C6A45),
        tassel = listOf(Color(0xFF9A5527), Color(0xFF6B3410), Color(0xFF4A220A)),
        band = listOf(Color(0xFFC49A52), Color(0xFF7A5428)),
    ),
)

@Composable
fun TasbihTheme(look: Look, content: @Composable () -> Unit) {
    val scheme = if (look.dark) {
        darkColorScheme(
            primary = look.accent, onPrimary = look.onAccent,
            primaryContainer = look.card, onPrimaryContainer = look.onBg,
            secondary = look.accent, secondaryContainer = look.line, onSecondaryContainer = look.onBg,
            background = look.bgTop, onBackground = look.onBg,
            surface = look.surface, onSurface = look.onBg,
            surfaceVariant = look.card, onSurfaceVariant = look.muted,
            outline = look.muted, outlineVariant = look.line,
        )
    } else {
        lightColorScheme(
            primary = look.accent, onPrimary = look.onAccent,
            primaryContainer = look.card, onPrimaryContainer = look.onBg,
            secondary = look.accent, secondaryContainer = look.line, onSecondaryContainer = look.onBg,
            background = look.bgTop, onBackground = look.onBg,
            surface = look.surface, onSurface = look.onBg,
            surfaceVariant = look.card, onSurfaceVariant = look.muted,
            outline = look.muted, outlineVariant = look.line,
        )
    }

    // status/navigation bar icons must stay readable on whichever look is active
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !look.dark
            isAppearanceLightNavigationBars = !look.dark
        }
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
