package pt.iade.ei.xplored

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.iade.ei.xplored.ui.theme.XploredTheme

val CardBackground = Color(0xFFF5F5F5)

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
                tint = if (isAction) Color.Red else Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = if (isAction) Color.Red else Color.Black,
                fontWeight = if (isAction) FontWeight.SemiBold else FontWeight.Normal
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 44.dp),
                thickness = 0.5.dp,
                color = Color.LightGray.copy(alpha = 0.5f)
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
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val scrollState = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                // Account
                SettingsSection(title = stringResource(R.string.settings_account))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = stringResource(R.string.settings_edit_profile),
                        onClick = { activity?.startActivity(Intent(activity, ProfileActivity::class.java)) }
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

                // Support & About
                SettingsSection(title = stringResource(R.string.settings_support_about))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Warning,
                        title = stringResource(R.string.settings_report_problem),
                        onClick = { /* keep as is */ }
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

                Spacer(modifier = Modifier.height(64.dp))
            }

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
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    XploredTheme { SettingsScreen() }
}
