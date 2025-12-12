package pt.iade.ei.xplored.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

// Fixed palette with enough contrast for white initials in both themes.
private val AVATAR_COLORS = listOf(
    Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFE53935), Color(0xFF8E24AA),
    Color(0xFFFB8C00), Color(0xFF00897B), Color(0xFF3949AB), Color(0xFFD81B60)
)

// Avatar that falls back to the user's initials on a coloured circle when the
// photo is missing or fails to load.
@Composable
fun InitialsAvatar(
    name: String?,
    photoUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    borderColor: Color? = null
) {
    val initials = remember(name) { initialsFor(name) }
    val background = remember(name) { avatarColorFor(name) }

    var boxModifier = modifier.size(size).clip(CircleShape).background(background)
    if (borderColor != null) boxModifier = boxModifier.border(2.dp, borderColor, CircleShape)

    Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
        if (!photoUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().clip(CircleShape),
                loading = { InitialsContent(initials, size) },
                error = { InitialsContent(initials, size) }
            )
        } else {
            InitialsContent(initials, size)
        }
    }
}

@Composable
private fun InitialsContent(initials: String, size: Dp) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.4f).sp
        )
    }
}

private fun avatarColorFor(name: String?): Color {
    if (name.isNullOrBlank()) return AVATAR_COLORS[0]
    val index = ((name.hashCode() % AVATAR_COLORS.size) + AVATAR_COLORS.size) % AVATAR_COLORS.size
    return AVATAR_COLORS[index]
}

private fun initialsFor(name: String?): String {
    val trimmed = name?.trim().orEmpty()
    if (trimmed.isEmpty()) return "?"
    val parts = trimmed.split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}
