package pt.iade.ei.xplored

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import pt.iade.ei.xplored.models.PlaceRepository
import pt.iade.ei.xplored.ui.theme.XploredTheme

/* ---------- Colors ---------- */
private val SecondaryColor = Color(0xFFE0F2F1) // light detail background

/* ---------- Data ---------- */
/** Single place/checkpoint inside a challenge. */
data class PedipaperPlace(val name: String, val description: String)

/** One Pedipaper challenge card. */
data class PedipaperChallenge(
    val title: String,
    val tagColor: Color,
    val places: List<PedipaperPlace>
)

/* ---------- Category → Tag Color mapping (matches MainActivity category colors) ---------- */
private fun categoryTagColor(context: android.content.Context, category: String): Color {
    val historicos   = context.getString(R.string.category_historicos)
    val paisagens    = context.getString(R.string.category_paisagens)
    val atividades   = context.getString(R.string.category_atividades)
    val lojas        = context.getString(R.string.category_lojas)
    val restauracao  = context.getString(R.string.category_restauracao)

    return when (category) {
        atividades  -> Color(0xFF4192FF) // blue  (MainActivity chip for Atividades)
        lojas       -> Color(0xFFFFEB3B) // yellow (Lojas)
        restauracao -> Color(0xFFE65100) // orange (Restauração)
        historicos  -> Color(0xFF880E4F) // magenta/wine (Históricos)
        paisagens   -> Color(0xFF3D6E44) // green  (Paisagens)
        else        -> Color(0xFFE53935) // red fallback
    }
}

/* ---------- Build challenges from repository places ---------- */
private fun buildChallengesFromPlaces(
    context: android.content.Context,
    groupSize: Int = 3
): List<PedipaperChallenge> {
    // Ensure repo is inited
    PlaceRepository.initialize(context)
    val places = PlaceRepository.getPlaces()
    if (places.isEmpty()) return emptyList()

    fun makeChallenges(from: List<pt.iade.ei.xplored.models.Place>): List<PedipaperChallenge> {
        val byCategory = from.groupBy { it.category }
        val out = mutableListOf<PedipaperChallenge>()

        byCategory.forEach { (category, items) ->
            val chunks = items.chunked(groupSize)
            chunks.forEachIndexed { idx, chunk ->
                val title =
                    if (chunks.size > 1)
                        context.getString(R.string.route_numbered, category, idx + 1)
                    else
                        category.ifBlank { context.getString(R.string.route_base) }

                val tag = categoryTagColor(context, category)
                val pedipaperPlaces = chunk.map { p ->
                    PedipaperPlace(name = p.name, description = p.description)
                }

                if (pedipaperPlaces.isNotEmpty()) {
                    out += PedipaperChallenge(
                        title = title,
                        tagColor = tag,
                        places = pedipaperPlaces
                    )
                }
            }
        }
        return out.sortedBy { it.title.lowercase() }
    }

    // Try per-category first
    val firstPass = makeChallenges(places)
    if (firstPass.isNotEmpty()) return firstPass

    // Fallback: chunk all places into generic routes
    val chunks = places.chunked(groupSize)
    return chunks.mapIndexed { idx, chunk ->
        PedipaperChallenge(
            title = context.getString(R.string.route_generic_numbered, idx + 1),
            tagColor = categoryTagColor(context, ""),
            places = chunk.map { p -> PedipaperPlace(p.name, p.description) }
        )
    }
}

/* ---------- Activity ---------- */
class PedipaperActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XploredTheme {
                PedipaperScreen()
            }
        }
    }
}

/* ---------- Dialog (challenge details) ---------- */
@Composable
fun ChallengeDetailDialog(challenge: PedipaperChallenge, onClose: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
                .height(IntrinsicSize.Min),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Title + close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        challenge.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DarkText
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.pedipaper_dialog_places_title),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(12.dp))

                challenge.places.forEachIndexed { index, place ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(SecondaryColor, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(R.string.cd_location),
                                tint = challenge.tagColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${index + 1}. ${place.name}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = place.description,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(context, MainActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkText)
                ) {
                    Text(stringResource(R.string.pedipaper_btn_start), fontSize = 18.sp)
                }
            }
        }
    }
}

/* ---------- Challenge card ---------- */
@Composable
fun ChallengeCard(challenge: PedipaperChallenge, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(bottom = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left color tag
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(challenge.tagColor),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = challenge.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.pedipaper_card_places_included),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

                challenge.places.take(3).forEach { place ->
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(challenge.tagColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = place.name,
                            fontSize = 16.sp,
                            color = DarkText
                        )
                    }
                }
            }
        }
    }
}

/* ---------- Screen ---------- */
@Composable
fun PedipaperScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedChallenge by remember { mutableStateOf<PedipaperChallenge?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F0F5)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(top = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(R.string.pedipaper_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                )

                Text(
                    text = stringResource(R.string.pedipaper_subtitle),
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                        .padding(top = 4.dp, bottom = 24.dp)
                )

                val challenges by remember(context) {
                    derivedStateOf { buildChallengesFromPlaces(context) }
                }

                challenges.forEach { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        onClick = { selectedChallenge = challenge }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Back button
            IconButton(
                onClick = { (context as? ComponentActivity)?.finish() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 32.dp, start = 8.dp)
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = Color.Black
                )
            }
        }
    }

    // Dialog
    selectedChallenge?.let { challenge ->
        ChallengeDetailDialog(challenge = challenge) { selectedChallenge = null }
    }
}

/* ---------- Preview ---------- */
@Preview(showBackground = true)
@Composable
fun PedipaperScreenPreview() {
    XploredTheme { PedipaperScreen() }
}
