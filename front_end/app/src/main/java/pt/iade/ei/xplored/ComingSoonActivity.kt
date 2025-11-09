package pt.iade.ei.xplored

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pt.iade.ei.xplored.ui.theme.XploredTheme

class ComingSoonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XploredTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ComingSoonScreen(onBack = { finish() })
                }
            }
        }
    }
}

@Composable
fun ComingSoonScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // invisible full-screen button: no ripple, captures any tap
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onBack() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.coming_soon_text),
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ComingSoonPreview() {
    XploredTheme { ComingSoonScreen(onBack = {}) }
}
