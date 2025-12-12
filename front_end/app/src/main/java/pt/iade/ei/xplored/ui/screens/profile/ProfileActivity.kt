package pt.iade.ei.xplored.ui.screens.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.data.models.reviews.Review
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.PhotoApiService
import pt.iade.ei.xplored.network.PhotoUploadClient
import pt.iade.ei.xplored.network.ReactionRequest
import pt.iade.ei.xplored.network.ReviewApiService
import pt.iade.ei.xplored.network.UserApiService
import pt.iade.ei.xplored.network.VisitApiService
import pt.iade.ei.xplored.repositories.PhotoRepository
import pt.iade.ei.xplored.ui.components.InitialsAvatar
import pt.iade.ei.xplored.ui.screens.favorites.FavoritesActivity
import pt.iade.ei.xplored.ui.screens.history.HistoryActivity
import pt.iade.ei.xplored.ui.screens.leaderboard.LeaderboardActivity
import pt.iade.ei.xplored.ui.screens.settings.SettingsActivity
import pt.iade.ei.xplored.ui.theme.XploredTheme
import java.util.UUID

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            XploredTheme {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current

                // --- FIX: Mutable State that updates on Resume ---
                var name by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var points by remember { mutableIntStateOf(0) }
                var about by remember { mutableStateOf("") }
                var country by remember { mutableStateOf("") }
                var isLoggedIn by remember { mutableStateOf(false) }
                var visitCount by remember { mutableIntStateOf(0) }
                // Bumped on every resume so the visit counter is refetched.
                var resumeTick by remember { mutableIntStateOf(0) }

                // This function reloads data from SessionManager
                fun refreshData() {
                    isLoggedIn = SessionManager.isLoggedIn(context)
                    name = SessionManager.getUserName(context)
                    email = SessionManager.getUserEmail(context)
                    points = SessionManager.getUserPoints(context)
                    about = SessionManager.getUserAbout(context)
                    country = SessionManager.getUserCountry(context)
                }

                // Listen for "ON_RESUME" (when you come back from Edit Screen)
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            refreshData()
                            resumeTick++
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // Number of places the user checked in at, straight from the server.
                LaunchedEffect(resumeTick, email) {
                    if (email.isNotBlank()) {
                        try {
                            visitCount = ApiClient.instance.create(VisitApiService::class.java)
                                .getVisitCount(email).visitCount
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                ProfileScreen(
                    userName = name.ifBlank { getString(R.string.user_fallback_name) },
                    userEmail = if (email.isNotBlank()) email else if (isLoggedIn) getString(R.string.em_dash) else "",
                    userPoints = points,
                    userAbout = about,
                    userCountry = country, // Pass the country
                    userVisits = visitCount,
                    onBack = { finish() },
                    onOpenSettings = {
                        runCatching { startActivity(Intent(context, SettingsActivity::class.java)) }
                    },
                    onOpenHistory = {
                        runCatching { startActivity(Intent(context, HistoryActivity::class.java)) }
                    },
                    onOpenFavorites = {
                        runCatching { startActivity(Intent(context, FavoritesActivity::class.java)) }
                    },
                    onOpenLeaderboard = {
                        runCatching { startActivity(Intent(context, LeaderboardActivity::class.java)) }
                    }
                )
            }
        }
    }
}

private enum class ProfileTab { Sobre, Reviews, Fotos }

@Composable
private fun ProfileScreen(
    userName: String,
    userEmail: String,
    userPoints: Int,
    userAbout: String,   // NEW Param
    userCountry: String, // NEW Param
    userVisits: Int,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(ProfileTab.Sobre) }

    val userId = remember(userEmail) {
        if (userEmail.isNotBlank()) stableUserId(userEmail) else ""
    }

    var profilePhoto by remember(userId) {
        mutableStateOf(PhotoRepository.getAvatarByUserId(ctx, userId)?.url?.let(Uri::parse))
    }

    LaunchedEffect(userEmail) {
        if (userEmail.isNotBlank()) {
            try {
                val api = ApiClient.instance.create(UserApiService::class.java)
                val user = api.getUserByEmail(userEmail)
                if (!user.profilePhoto.isNullOrBlank()) {
                    profilePhoto = Uri.parse(user.profilePhoto)
                    PhotoRepository.upsertAvatar(ctx, userId, user.profilePhoto)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && userId.isNotBlank()) {
            profilePhoto = uri
            PhotoRepository.upsertAvatar(ctx, userId, uri.toString())

            scope.launch {
                PhotoUploadClient.uploadPhoto(
                    context = ctx,
                    uri = uri,
                    userId = userEmail,
                    placeId = "",
                    reviewId = "AVATAR-UPDATE"
                )
            }
        }
    }

    var fullScreenUri by remember { mutableStateOf<Uri?>(null) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 44.dp, start = 8.dp, end = 8.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(padding)
        ) {
            // --- HEADER SECTION ---
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                    InitialsAvatar(
                        name = userName,
                        photoUrl = profilePhoto?.toString(),
                        size = 132.dp,
                        borderColor = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(onClickLabel = stringResource(R.string.cd_edit_avatar)) {
                                if (profilePhoto != null) fullScreenUri = profilePhoto
                                else pickAvatarLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    )

                    AvatarEditButton(
                        onClick = {
                            pickAvatarLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(userName, fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurface)

                // --- NEW: Display Country if available ---
                if (userCountry.isNotBlank()) {
                    Text(userCountry, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                } else if (userEmail.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(userEmail, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // -----------------------------------------

                Spacer(Modifier.height(8.dp))
                // Points chip with the visited places stat beside it.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.points_label, userPoints)) })
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.visited_places_label, userVisits),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Shortcut rows (icon, label, chevron) inside a rounded card.
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    ProfileLinkRow(
                        icon = Icons.Default.EmojiEvents,
                        iconTint = Color(0xFFFFC107),
                        label = stringResource(R.string.leaderboard_title),
                        onClick = onOpenLeaderboard
                    )
                    ProfileLinkRow(
                        icon = Icons.Default.Star,
                        iconTint = Color(0xFFFFCC66),
                        label = stringResource(R.string.history_title),
                        onClick = onOpenHistory
                    )
                    ProfileLinkRow(
                        icon = Icons.Default.Favorite,
                        iconTint = Color(0xFFE53935),
                        label = stringResource(R.string.favorites_title),
                        onClick = onOpenFavorites,
                        showDivider = false
                    )
                }

                Spacer(Modifier.height(8.dp))
            }

            // --- TABS ---
            val tabs = listOf(ProfileTab.Sobre, ProfileTab.Reviews, ProfileTab.Fotos)

            TabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { positions ->
                    val idx = tabs.indexOf(selectedTab)
                    if (idx < positions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(positions[idx]),
                            color = Color(0xFFFF7A80),
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                when (tab) {
                                    ProfileTab.Sobre -> stringResource(R.string.tab_about)
                                    ProfileTab.Reviews -> stringResource(R.string.tab_reviews)
                                    ProfileTab.Fotos -> stringResource(R.string.tab_photos)
                                },
                                fontSize = 18.sp
                            )
                        }
                    )
                }
            }

            // --- TAB CONTENT ---
            when (selectedTab) {
                ProfileTab.Sobre -> {
                    // NEW: Read-Only View of About
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.profile_about_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Use the passed parameter 'userAbout' which auto-refreshes
                        val displayText = userAbout.ifBlank {
                            if (userEmail.isNotBlank()) SessionManager.getAboutForEmail(ctx, userEmail) else ""
                        }

                        if (displayText.isNotBlank()) {
                            Text(
                                text = displayText,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 24.sp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.profile_no_about),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }

                ProfileTab.Reviews -> {
                    // (Reviews code remains exactly the same as previous batches,
                    //  fetching from API when userEmail is present)
                    var userReviews by remember { mutableStateOf<List<Review>>(emptyList()) }
                    var isLoading by remember { mutableStateOf(true) }

                    val fetchReviews = {
                        if (userEmail.isNotBlank()) {
                            scope.launch {
                                try {
                                    val api = ApiClient.instance.create(ReviewApiService::class.java)
                                    userReviews = api.getReviewsByUserEmail(userEmail)
                                } catch (e: Exception) { e.printStackTrace() }
                                finally { isLoading = false }
                            }
                        } else { isLoading = false }
                    }

                    LaunchedEffect(userEmail) { fetchReviews() }

                    if (userReviews.isEmpty() && !isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.profile_no_reviews), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(userReviews) { review ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!review.placeCoverUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = review.placeCoverUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(Icons.Default.Place, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(review.placeName ?: "Local", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(review.createdAt?.take(10) ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row {
                                            repeat(5) { index ->
                                                val isActive = index < review.rating
                                                Icon(
                                                    imageVector = if (isActive) Icons.Filled.Star else Icons.Outlined.Star,
                                                    contentDescription = null,
                                                    tint = if (isActive) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        if (!review.title.isNullOrBlank()) Text(review.title, fontWeight = FontWeight.SemiBold)
                                        if (!review.comment.isNullOrBlank()) Text(review.comment, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                ProfileTab.Fotos -> {
                    // NEW: Read-only Gallery (Uploads removed)
                    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }
                    var isLoading by remember { mutableStateOf(true) }

                    LaunchedEffect(userEmail) {
                        if (userEmail.isNotBlank()) {
                            scope.launch {
                                try {
                                    val api = ApiClient.instance.create(PhotoApiService::class.java)
                                    val serverList = api.getPhotosByUser(userEmail)
                                    photos = serverList
                                        .filter { it.url != null && (it.reviewId == null || !it.reviewId.startsWith("AVATAR")) }
                                        .map { Uri.parse(it.url) }
                                        .reversed()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else { isLoading = false }
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (photos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.profile_no_photos), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(photos) { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { fullScreenUri = uri }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (fullScreenUri != null) {
            Dialog(
                onDismissRequest = { fullScreenUri = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black)
                        .clickable { fullScreenUri = null }
                ) {
                    AsyncImage(
                        model = fullScreenUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { fullScreenUri = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                    ) {
                        Icon(Icons.Filled.Close, stringResource(R.string.cd_close), tint = Color.White)
                    }
                }
            }
        }
    }
}

// Tappable shortcut row: coloured icon, label and a chevron.
@Composable
private fun ProfileLinkRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
            Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 52.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
private fun AvatarEditButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Box(modifier = Modifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Edit, stringResource(R.string.cd_edit_avatar), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun stableUserId(email: String): String = UUID.nameUUIDFromBytes(email.lowercase().toByteArray()).toString()

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    XploredTheme {
        ProfileScreen("Tiago", "tiago@test.com", 1250, "About me...", "Portugal", 7, {}, {}, {}, {}, {})
    }
}