package pt.iade.ei.xplored

import android.content.Context
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import pt.iade.ei.xplored.models.PhotoItem
import pt.iade.ei.xplored.models.PhotoRepository
import pt.iade.ei.xplored.models.PhotoStatus
import pt.iade.ei.xplored.ui.theme.XploredTheme
import java.util.UUID
import androidx.core.net.toUri

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XploredTheme {
                val ctx = this

                val isLoggedIn = remember { SessionManager.isLoggedIn(ctx) }
                val name = remember { SessionManager.getUserName(ctx) }
                val email = remember { SessionManager.getUserEmail(ctx) }
                val points = remember { SessionManager.getUserPoints(ctx) }

                ProfileScreen(
                    userName = name.ifBlank { getString(R.string.user_fallback_name) },
                    userEmail = if (email.isNotBlank()) email else if (isLoggedIn) getString(R.string.em_dash) else "",
                    userPoints = points,
                    onBack = { finish() },
                    onOpenSettings = {
                        runCatching { startActivity(Intent(this, SettingsActivity::class.java)) }
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
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val ctx = LocalContext.current
    var selectedTab by remember { mutableStateOf(ProfileTab.Fotos) }

    // Stable userId for DB/repo lookups (keeps UI identical)
    val userId = remember(userEmail) {
        if (userEmail.isNotBlank()) stableUserId(userEmail) else ""
    }

    // ---------- Avatar state via PhotoRepository (kind = AVATAR) ----------
    var profilePhoto by remember(userId) {
        mutableStateOf(PhotoRepository.getAvatarByUserId(ctx, userId)?.url?.let(Uri::parse))
    }

    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && userId.isNotBlank()) {
            profilePhoto = uri
            PhotoRepository.upsertAvatar(ctx, userId, uri.toString())
        }
    }

    // Fullscreen image state for avatar/gallery
    var fullScreenUri by remember { mutableStateOf<Uri?>(null) }

    // ---------- About text (persist to SessionManager like before) ----------
    var aboutText by rememberSaveable {
        mutableStateOf(
            SessionManager.getUserAbout(ctx).ifBlank {
                if (userEmail.isNotBlank()) SessionManager.getAboutForEmail(ctx, userEmail) else ""
            }
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 44.dp, start = 8.dp, end = 8.dp)
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
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            // ---------- Avatar + identity ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Outer box (no clip) so edit button sits in front
                Box(
                    modifier = Modifier.size(132.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner circular avatar
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFE2E6EF), CircleShape)
                            .background(Color(0xFFEDEFF2))
                            .clickable {
                                // Tap avatar -> open full screen if exists, else open picker
                                if (profilePhoto != null) {
                                    fullScreenUri = profilePhoto
                                } else {
                                    pickAvatarLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            }
                    ) {
                        if (profilePhoto != null) {
                            AsyncImage(
                                model = profilePhoto,
                                contentDescription = stringResource(R.string.cd_profile_photo),
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = stringResource(R.string.cd_profile),
                                tint = Color(0xFF6B7280),
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }

                    // Edit button overlay
                    AvatarEditButton(
                        onClick = {
                            pickAvatarLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(userName, fontSize = 26.sp, color = Color(0xFF1E2835))
                if (userEmail.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(userEmail, fontSize = 14.sp, color = Color(0xFF6B7280))
                }

                var points by rememberSaveable { mutableIntStateOf(userPoints) }
                Spacer(Modifier.height(8.dp))
                AssistChip(onClick = {}, label = { Text(stringResource(R.string.points_label, points)) })

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val newPts = points + 50
                        if (userEmail.isNotBlank())
                            SessionManager.updateUserPoints(ctx, userEmail, newPts)
                        else
                            SessionManager.saveUserPoints(ctx, newPts)
                        points = newPts
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2AC6B2),
                        contentColor = Color.White
                    )
                ) { Text(stringResource(R.string.add_points_debug)) }

                Spacer(Modifier.height(8.dp))
            }

            // ---------- Tabs ----------
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

            // ---------- Content ----------
            when (selectedTab) {
                ProfileTab.Sobre -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = aboutText,
                            onValueChange = { newText ->
                                aboutText = newText
                                if (userEmail.isNotBlank()) {
                                    SessionManager.updateUserAbout(ctx, userEmail, newText)
                                } else {
                                    SessionManager.saveUserAbout(ctx, newText)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            placeholder = { Text(stringResource(R.string.about_placeholder)) },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 8,
                            maxLines = Int.MAX_VALUE
                        )
                    }
                }
                ProfileTab.Reviews -> {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White))
                }
                ProfileTab.Fotos -> {
                    // Gallery photos from PhotoRepository (kind = GALLERY)
                    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }

                    LaunchedEffect(userId) {
                        photos = if (userId.isNotBlank()) {
                            PhotoRepository.getGalleryByUserId(ctx, userId).map { it.url.toUri() }
                        } else emptyList()
                    }

                    val pickGalleryPhoto = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri: Uri? ->
                        if (uri != null && userId.isNotBlank()) {
                            PhotoRepository.insert(
                                ctx,
                                PhotoItem(
                                    reviewId = "PROFILE-$userId",
                                    placeId = null,
                                    userId = userId,
                                    url = uri.toString(),
                                    status = PhotoStatus.APPROVED
                                )
                            )
                            // Reload
                            photos = PhotoRepository.getGalleryByUserId(ctx, userId).map { it.url.toUri() }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Photo items
                        items(photos) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = stringResource(R.string.cd_photo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { fullScreenUri = uri }
                            )
                        }

                        // "+" tile
                        item {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFEDEFF2))
                                    .border(1.dp, Color(0xFFE2E6EF), RoundedCornerShape(10.dp))
                                    .clickable {
                                        pickGalleryPhoto.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.plus_sign),
                                    fontSize = 28.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------- Fullscreen image viewer (avatar + gallery) ----------
        if (fullScreenUri != null) {
            Dialog(
                onDismissRequest = { fullScreenUri = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
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
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_close_menu),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/* ---------- presentable edit badge for avatar ---------- */
@Composable
private fun AvatarEditButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .clickable(onClick = onClick)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = Color(0xFF1E2835)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.cd_profile_photo),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/* ---------- stable userId helper (swap to real DB id later) ---------- */
private fun stableUserId(email: String): String =
    UUID.nameUUIDFromBytes(email.lowercase().toByteArray()).toString()

/* ---------- PREVIEW ---------- */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    XploredTheme {
        ProfileScreen(
            userName = "Tiago Cabaça",
            userEmail = "tiago@xplored.app",
            userPoints = 1250,
            onBack = {},
            onOpenSettings = {}
        )
    }
}
