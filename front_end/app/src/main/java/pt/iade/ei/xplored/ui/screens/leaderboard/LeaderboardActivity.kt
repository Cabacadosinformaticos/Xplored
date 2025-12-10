package pt.iade.ei.xplored.ui.screens.leaderboard

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.LeaderboardApiService
import pt.iade.ei.xplored.network.LeaderboardEntry
import pt.iade.ei.xplored.network.LeaderboardMe
import pt.iade.ei.xplored.ui.theme.XploredTheme

// Same palette as the rest of the app; podium uses gold, silver and bronze.
private val DarkText = Color(0xFF1E2835)
private val CardBackground = Color(0xFFF8F9FA)
private val Gold = Color(0xFFFFC107)
private val Silver = Color(0xFFB0BEC5)
private val Bronze = Color(0xFFBCAAA4)
private val HighlightBackground = Color(0xFFFFF3CD)

class LeaderboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XploredTheme {
                LeaderboardScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaderboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var top by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var me by remember { mutableStateOf<LeaderboardMe?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val email = SessionManager.getUserEmail(context)
        if (email.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val response = ApiClient.instance.create(LeaderboardApiService::class.java)
                .getLeaderboard(email)
            top = response.top
            me = response.me
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.leaderboard_error), Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.leaderboard_title), fontWeight = FontWeight.Bold, color = DarkText)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (top.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.leaderboard_empty), color = Color.Gray)
                }
            } else {
                val podium = top.filter { it.position <= 3 }
                val rest = top.filter { it.position > 3 }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { PodiumRow(podium, me?.position) }

                    items(rest) { entry ->
                        LeaderboardRow(
                            position = entry.position,
                            name = entry.name,
                            points = entry.points,
                            photo = entry.profilePhoto,
                            isMe = me != null && me?.position == entry.position
                        )
                    }

                    // Own position shown on its own when it falls outside the top 10.
                    val own = me
                    if (own != null && own.position > 10) {
                        item {
                            Column {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.leaderboard_your_position),
                                    fontWeight = FontWeight.Bold,
                                    color = DarkText,
                                    fontSize = 16.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                LeaderboardRow(
                                    position = own.position,
                                    name = own.name,
                                    points = own.points,
                                    photo = null,
                                    isMe = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Top 3 podium, ordered 2nd, 1st, 3rd so the winner stands in the middle.
@Composable
private fun PodiumRow(podium: List<LeaderboardEntry>, myPosition: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(2, 1, 3).forEach { position ->
            val entry = podium.firstOrNull { it.position == position }
            if (entry != null) {
                PodiumItem(
                    entry = entry,
                    isMe = myPosition == entry.position,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PodiumItem(entry: LeaderboardEntry, isMe: Boolean, modifier: Modifier = Modifier) {
    val medalColor = when (entry.position) {
        1 -> Gold
        2 -> Silver
        else -> Bronze
    }
    val blockHeight = when (entry.position) {
        1 -> 92.dp
        2 -> 70.dp
        else -> 56.dp
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Avatar(photo = entry.profilePhoto, borderColor = medalColor)
        Spacer(Modifier.height(6.dp))
        Text(
            text = entry.name,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = DarkText,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Text(stringResource(R.string.points_label, entry.points), fontSize = 12.sp, color = Color.Gray)
        if (isMe) {
            Text(
                stringResource(R.string.leaderboard_you),
                fontSize = 12.sp,
                color = Color(0xFF8A6D3B),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(blockHeight)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(medalColor),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "${entry.position}",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    position: Int,
    name: String,
    points: Int,
    photo: String?,
    isMe: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) HighlightBackground else CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMe) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DarkText,
                modifier = Modifier.width(28.dp)
            )
            Avatar(photo = photo, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = DarkText, maxLines = 1)
                if (isMe) {
                    Text(
                        stringResource(R.string.leaderboard_you),
                        fontSize = 12.sp,
                        color = Color(0xFF8A6D3B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (position <= 3) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(stringResource(R.string.points_label, points), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkText)
            }
        }
    }
}

@Composable
private fun Avatar(photo: String?, size: androidx.compose.ui.unit.Dp = 56.dp, borderColor: Color = Color(0xFFE2E6EF)) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .background(Color(0xFFEDEFF2)),
        contentAlignment = Alignment.Center
    ) {
        if (!photo.isNullOrBlank()) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                modifier = Modifier.matchParentSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
