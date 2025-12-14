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
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import pt.iade.ei.xplored.repositories.PhotoRepository
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
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                ProfileScreen(
                    userName = name.ifBlank { getString(R.string.user_fallback_name) },
                    userEmail = if (email.isNotBlank()) email else if (isLoggedIn) getString(R.string.em_dash) else "",
                    userPoints = points,
                    userAbout = about,
                    userCountry = country, // Pass the country
                    onBack = { finish() },
                    onOpenSettings = {
                        runCatching { startActivity(Intent(context, SettingsActivity::class.java)) }
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
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(ProfileTab.Fotos) }

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
                if (!user.profilePhotoUrl.isNullOrBlank()) {
                    profilePhoto = Uri.parse(user.profilePhotoUrl)
                    PhotoRepository.upsertAvatar(ctx, userId, user.profilePhotoUrl)
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
                        tint = Color.Black
                    )
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.cd_settings),
                        tint = Color.Black
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier.fillMaxSize().background(Color.White).padding(padding)
        ) {
            // --- HEADER SECTION ---
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFE2E6EF), CircleShape)
                            .background(Color(0xFFEDEFF2))
                            .clickable {
                                if (profilePhoto != null) fullScreenUri = profilePhoto
                                else pickAvatarLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        if (profilePhoto != null) {
                            AsyncImage(
                                model = profilePhoto,
                                contentDescription = stringResource(R.string.cd_profile_photo),
                                modifier = Modifier.matchParentSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.cd_profile),
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(64.dp).align(Alignment.Center)
                            )
                        }
                    }

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
                Text(userName, fontSize = 26.sp, color = Color(0xFF1E2835))

                // --- NEW: Display Country if available ---
                if (userCountry.isNotBlank()) {
                    Text(userCountry, fontSize = 14.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
                } else if (userEmail.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(userEmail, fontSize = 14.sp, color = Color(0xFF6B7280))
                }
                // -----------------------------------------

                var points by rememberSaveable { mutableIntStateOf(userPoints) }
                // Keep points updated when coming back from game
                LaunchedEffect(userPoints) { points = userPoints }

                Spacer(Modifier.height(8.dp))
                AssistChip(onClick = {}, label = { Text(stringResource(R.string.points_label, points)) })

                Spacer(Modifier.height(8.dp))

                // DEBUG BUTTON
                Button(
                    onClick = {
                        val newPts = points + 50
                        points = newPts
                        if (userEmail.isNotBlank()) {
                            SessionManager.updateUserPoints(ctx, userEmail, newPts)
                            scope.launch {
                                try {
                                    val api = ApiClient.instance.create(UserApiService::class.java)
                                    api.updatePoints(userEmail, newPts)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } else {
                            SessionManager.saveUserPoints(ctx, newPts)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2AC6B2),
                        contentColor = Color.White
                    )
                ) { Text(stringResource(R.string.add_points_debug)) }

                Spacer(Modifier.height(8.dp))
            }

            // --- TABS ---
            val tabs = listOf(ProfileTab.Sobre, ProfileTab.Reviews, ProfileTab.Fotos)

            TabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                containerColor = Color.White,
                contentColor = Color(0xFF1E2835),
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
                        selectedContentColor = Color(0xFF1E2835),
                        unselectedContentColor = Color(0xFF9AA3AE),
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
                            text = "Sobre Mim",
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
                                color = Color(0xFF4A4A4A),
                                lineHeight = 24.sp
                            )
                        } else {
                            Text(
                                text = "Ainda sem descrição.",
                                color = Color.Gray,
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
                            Text("Ainda não escreveste avaliações.", color = Color.Gray)
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
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
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
                                                Icon(Icons.Default.Place, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(review.placeName ?: "Local", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(review.createdAt?.take(10) ?: "", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row {
                                            repeat(5) { index ->
                                                val isActive = index < review.rating
                                                Icon(
                                                    imageVector = if (isActive) Icons.Filled.Star else Icons.Outlined.Star,
                                                    contentDescription = null,
                                                    tint = if (isActive) Color(0xFFFFC107) else Color.LightGray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        if (!review.title.isNullOrBlank()) Text(review.title, fontWeight = FontWeight.SemiBold)
                                        if (!review.comment.isNullOrBlank()) Text(review.comment, fontSize = 14.sp)

                                        // Reactions
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { /* Toggle Logic */ }) {
                                                Icon(
                                                    if (review.currentUserReaction == "USEFUL") Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                                    null,
                                                    tint = if (review.currentUserReaction == "USEFUL") MaterialTheme.colorScheme.primary else Color.Gray
                                                )
                                            }
                                            Text("${review.likesCount}", fontSize = 14.sp, color = Color.Gray)
                                            Spacer(Modifier.width(16.dp))
                                            IconButton(onClick = { /* Toggle Logic */ }) {
                                                Icon(
                                                    if (review.currentUserReaction == "NOT_USEFUL") Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                                    null,
                                                    tint = if (review.currentUserReaction == "NOT_USEFUL") Color.Red else Color.Gray
                                                )
                                            }
                                            Text("${review.dislikesCount}", fontSize = 14.sp, color = Color.Gray)
                                        }
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
                            Text("Sem fotos na galeria.", color = Color.Gray)
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
                        Icon(Icons.Filled.Close, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarEditButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.size(40.dp), shape = CircleShape, color = Color.White, shadowElevation = 8.dp) {
        Box(modifier = Modifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = Color(0xFF1E2835)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
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
        ProfileScreen("Tiago", "tiago@test.com", 1250, "About me...", "Portugal", {}, {})
    }
}