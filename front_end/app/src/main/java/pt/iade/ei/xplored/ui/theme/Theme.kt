package pt.iade.ei.xplored.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun XploredTheme(content: @Composable () -> Unit) {
    // Dark mode is not implemented yet and the screens assume light surfaces,
    // so the light scheme is forced for now.
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
