package pt.iade.ei.xplored.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import pt.iade.ei.xplored.R

// Light brand scheme: navy primary, purple secondary, white surfaces.
private val LightColorScheme = lightColorScheme(
    primary = BrandNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE3EC),
    onPrimaryContainer = BrandNavy,
    inversePrimary = BrandNavyLight,
    secondary = BrandPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF21005D),
    tertiary = BrandGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCDEBD2),
    onTertiaryContainer = Color(0xFF0B2A10),
    background = LightBackground,
    onBackground = BrandNavy,
    surface = LightSurface,
    onSurface = BrandNavy,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF6B7280),
    surfaceTint = BrandNavy,
    inverseSurface = Color(0xFF2C3138),
    inverseOnSurface = Color(0xFFF1F3F6),
    error = Color(0xFFC62828),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = LightOutline,
    outlineVariant = Color(0xFFEDEFF2),
    scrim = Color(0xFF000000)
)

// Dark brand scheme: same hues, lighter accents so text stays readable.
private val DarkColorScheme = darkColorScheme(
    primary = BrandNavyLight,
    onPrimary = BrandNavy,
    primaryContainer = Color(0xFF3A4658),
    onPrimaryContainer = Color(0xFFDCE4F0),
    inversePrimary = BrandNavy,
    secondary = BrandPurpleLight,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = BrandGreenLight,
    onTertiary = Color(0xFF10331A),
    tertiaryContainer = Color(0xFF2A4A31),
    onTertiaryContainer = Color(0xFFCDEBD2),
    background = DarkBackground,
    onBackground = Color(0xFFE3E7EE),
    surface = DarkSurface,
    onSurface = Color(0xFFE3E7EE),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B8C4),
    surfaceTint = BrandNavyLight,
    inverseSurface = Color(0xFFE3E7EE),
    inverseOnSurface = Color(0xFF2C3138),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = DarkOutline,
    outlineVariant = Color(0xFF2F3946),
    scrim = Color(0xFF000000)
)

/**
 * Theme mode stored by the Settings screen: "system", "light" or "dark".
 * Uses plain SharedPreferences, no extra dependency.
 */
object ThemePreferences {
    const val MODE_SYSTEM = "system"
    const val MODE_LIGHT = "light"
    const val MODE_DARK = "dark"

    private const val PREFS_NAME = "XploredTheme"
    private const val KEY_MODE = "theme_mode"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Snapshot state, so every open screen recomposes when the mode changes,
    // not only the Settings screen that gets recreated.
    private val mode = mutableStateOf<String?>(null)

    fun getMode(context: Context): String =
        mode.value ?: (prefs(context).getString(KEY_MODE, MODE_SYSTEM) ?: MODE_SYSTEM).also { mode.value = it }

    fun setMode(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_MODE, mode).apply()
        this.mode.value = mode
    }

    /** Resolves the stored mode against the system setting. */
    fun isDark(context: Context, systemDark: Boolean): Boolean = when (getMode(context)) {
        MODE_LIGHT -> false
        MODE_DARK -> true
        else -> systemDark
    }
}

@Composable
fun XploredTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = ThemePreferences.isDark(context, isSystemInDarkTheme())
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Keep the status and navigation bar icons readable in both modes.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = context.findActivity()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            window.setBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(
                    (if (darkTheme) DarkBackground else LightBackground).toArgb()
                )
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Orange used for point amounts and rewards. The light variant is the original
 * brand orange; dark mode needs a brighter one to stay readable.
 */
@Composable
fun pointsAccent(): Color {
    val context = LocalContext.current
    val darkTheme = ThemePreferences.isDark(context, isSystemInDarkTheme())
    return if (darkTheme) Color(0xFFFFB74D) else Color(0xFFE65100)
}

/** Raw resource of the Google Maps style that matches the active theme. */
@Composable
fun mapStyleResource(): Int {
    val context = LocalContext.current
    val darkTheme = ThemePreferences.isDark(context, isSystemInDarkTheme())
    return if (darkTheme) R.raw.map_style_dark else R.raw.map_style
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
