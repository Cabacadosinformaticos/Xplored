package pt.iade.ei.xplored.ui.screens.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.ui.screens.admin.ModerationActivity
import pt.iade.ei.xplored.ui.screens.auth.LoginNewUserActivity
import pt.iade.ei.xplored.ui.screens.auth.LoginWelcomeBackActivity
import pt.iade.ei.xplored.ui.screens.main.MainActivity
import pt.iade.ei.xplored.ui.screens.misc.ComingSoonActivity
import pt.iade.ei.xplored.ui.screens.profile.EditProfileActivity
import pt.iade.ei.xplored.ui.theme.ThemePreferences
import pt.iade.ei.xplored.ui.theme.XploredTheme

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XploredTheme { SettingsScreen() }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isAction: Boolean = false,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAction) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = if (isAction) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isAction) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 44.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val scrollState = rememberScrollState()
    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Account
                SettingsSection(title = stringResource(R.string.settings_account))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    // KEEPING THIS: Opens EditProfileActivity
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = stringResource(R.string.settings_edit_profile),
                        onClick = { activity?.startActivity(Intent(activity, EditProfileActivity::class.java)) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = stringResource(R.string.settings_security),
                        onClick = { activity?.startActivity(Intent(activity, ComingSoonActivity::class.java)) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_notifications),
                        onClick = { activity?.startActivity(Intent(activity, ComingSoonActivity::class.java)) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.settings_privacy),
                        onClick = { activity?.startActivity(Intent(activity, ComingSoonActivity::class.java)) },
                        showDivider = false
                    )
                }

                // Moderation: only the ADMIN role sees this section.
                if (SessionManager.getUserRole(context).equals("ADMIN", ignoreCase = true)) {
                    SettingsSection(title = stringResource(R.string.settings_admin))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        SettingsItem(
                            icon = Icons.Default.VerifiedUser,
                            title = stringResource(R.string.settings_moderation),
                            onClick = { activity?.startActivity(Intent(activity, ModerationActivity::class.java)) },
                            showDivider = false
                        )
                    }
                }

                // Appearance: light, dark or follow the system.
                SettingsSection(title = stringResource(R.string.settings_appearance))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "${stringResource(R.string.settings_theme)}: ${themeModeLabel(context)}",
                        onClick = { showThemeDialog = true },
                        showDivider = false
                    )
                }

                // Support & About
                SettingsSection(title = stringResource(R.string.settings_support_about))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Description,
                        title = stringResource(R.string.settings_receipts),
                        onClick = { activity?.startActivity(Intent(activity, ComingSoonActivity::class.java)) }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = stringResource(R.string.settings_help_support),
                        onClick = { activity?.startActivity(Intent(activity, ComingSoonActivity::class.java)) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Policy,
                        title = stringResource(R.string.settings_terms_policies),
                        onClick = { activity?.startActivity(Intent(activity, ComingSoonActivity::class.java)) },
                        showDivider = false
                    )
                }

                // Map preferences
                SettingsSection(title = stringResource(R.string.settings_map_preferences))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Map,
                        title = stringResource(R.string.settings_maps),
                        onClick = {
                            activity?.let {
                                val intent = Intent(activity, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                activity.startActivity(intent)
                            }
                        }
                    )
                    SettingsItem(
                        icon = Icons.Default.OfflineBolt,
                        title = stringResource(R.string.settings_data_saver),
                        onClick = { activity?.startActivity(Intent(activity, ComingSoonActivity::class.java)) },
                        showDivider = false
                    )
                }

                // Actions
                SettingsSection(title = stringResource(R.string.settings_actions))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Warning,
                        title = stringResource(R.string.settings_report_problem),
                        onClick = { activity?.startActivity(Intent(activity, ComingSoonActivity::class.java)) }
                    )
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = stringResource(R.string.settings_add_account),
                        onClick = {
                            val intent = Intent(activity, LoginNewUserActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            activity?.startActivity(intent)
                        }
                    )
                    // KEEPING THIS: Logout Logic
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        title = stringResource(R.string.settings_logout),
                        isAction = true,
                        onClick = {
                            SessionManager.clearSession(context)
                            val intent = Intent(activity, LoginWelcomeBackActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            }
                            activity?.startActivity(intent)
                            activity?.finish()
                        },
                        showDivider = false
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showThemeDialog) {
            val currentMode = ThemePreferences.getMode(context)
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text(stringResource(R.string.theme_dialog_title)) },
                text = {
                    Column {
                        listOf(
                            ThemePreferences.MODE_SYSTEM to R.string.theme_system,
                            ThemePreferences.MODE_LIGHT to R.string.theme_light,
                            ThemePreferences.MODE_DARK to R.string.theme_dark
                        ).forEach { (mode, labelRes) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ThemePreferences.setMode(context, mode)
                                        showThemeDialog = false
                                        // Rebuild every screen with the new scheme.
                                        activity?.recreate()
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = currentMode == mode, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(stringResource(labelRes))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        }
    }
}

// Label shown in the Settings row for the stored theme mode.
@Composable
private fun themeModeLabel(context: Context): String = when (ThemePreferences.getMode(context)) {
    ThemePreferences.MODE_LIGHT -> stringResource(R.string.theme_light)
    ThemePreferences.MODE_DARK -> stringResource(R.string.theme_dark)
    else -> stringResource(R.string.theme_system)
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    XploredTheme { SettingsScreen() }
}