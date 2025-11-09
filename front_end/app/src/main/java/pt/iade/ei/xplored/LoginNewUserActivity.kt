package pt.iade.ei.xplored

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.iade.ei.xplored.models.User
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.UserApiService
import pt.iade.ei.xplored.ui.theme.XploredTheme

/**
 * Registration screen Activity entry point.
 */
class LoginNewUserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XploredTheme {
                RegistrationScreen()
            }
        }
    }
}

@Composable
fun RegistrationScreen() {
    // Form state
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? ComponentActivity

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Back to main map screen
            IconButton(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                },
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

            // Registration form container
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Top spacing aligned with other auth screens
                Spacer(modifier = Modifier.height(120.dp))

                // Headline
                Text(
                    text = stringResource(R.string.register_greeting_line1),
                    fontSize = 27.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.register_greeting_line2),
                    fontSize = 27.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp)
                )

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.register_username)) },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.login_email)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password with visibility toggle
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.login_password)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = stringResource(R.string.cd_toggle_password)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm password with visibility toggle
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.register_confirm_password)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = stringResource(R.string.cd_toggle_password)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Submit registration
                Button(
                    onClick = { performRegistration(context, username, email, password, confirmPassword) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2835))
                ) {
                    Text(text = stringResource(R.string.register_button), fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Link to existing account sign-in
                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.register_already_have_account),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = stringResource(R.string.register_login_now),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            val intent = Intent(context, LoginWelcomeBackActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Validates input, persists local user, triggers API creation, and navigates to Profile.
 */
private fun performRegistration(
    context: Context,
    username: String,
    email: String,
    password: String,
    confirmPassword: String
) {
    val minLength = 3
    Log.d("RegisterDebug", "Starting registration with username=$username, email=$email")

    // Basic presence check
    if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
        Toast.makeText(context, context.getString(R.string.err_fill_all_fields), Toast.LENGTH_SHORT).show()
        Log.d("RegisterDebug", "Fields missing")
        return
    }

    // Minimum length validation
    if (username.length <= minLength || email.length <= minLength || password.length <= minLength) {
        Toast.makeText(context, context.getString(R.string.err_fields_too_short), Toast.LENGTH_LONG).show()
        Log.d("RegisterDebug", "Fields too short")
        return
    }

    // Email domain allowlist
    val emailLower = email.trim().lowercase()
    val allowedSuffixes = listOf(
        "@gmail.com",
        "@hotmail.com",
        "@outlook.com",
        "@yahoo.com",
        "@icloud.com",
        "@iade.pt"
    )
    val emailAllowed = allowedSuffixes.any { emailLower.endsWith(it) }
    if (!emailAllowed) {
        Toast.makeText(context, context.getString(R.string.err_email_domain_not_allowed), Toast.LENGTH_LONG).show()
        Log.d("RegisterDebug", "Email domain not allowed: $emailLower")
        return
    }

    // Password confirmation
    if (password != confirmPassword) {
        Toast.makeText(context, context.getString(R.string.err_passwords_do_not_match), Toast.LENGTH_LONG).show()
        Log.d("RegisterDebug", "Passwords do not match")
        return
    }

    // Persist to local session (SharedPreferences)
    Log.d("RegisterDebug", "Adding user to SharedPreferences…")
    SessionManager.addRegisteredUser(context, username, email, password)
    SessionManager.setLoggedIn(context, true)
    SessionManager.saveUserData(context, username, email)
    SessionManager.saveUserPoints(context, 0)

    // Debug dump of stored users
    val users = SessionManager.getRegisteredUsersArray(context)
    Log.d("RegisterDebug", "Users after registration: $users")

    // UX feedback and navigation to profile
    Toast.makeText(context, context.getString(R.string.msg_registration_navigating_profile), Toast.LENGTH_SHORT).show()
    val intent = Intent(context, ProfileActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    context.startActivity(intent)

    // Remote API user creation (best-effort, non-blocking)
    val api = ApiClient.instance.create(UserApiService::class.java)
    val newUser = User(
        userId = null,
        name = username,
        email = email,
        about = "",
        points = 0,
        passwordHash = password
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val createdUser = api.createUser(newUser)
            Log.d("RegisterDebug", "User created successfully: $createdUser")

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.msg_registration_success_welcome, createdUser.name),
                    Toast.LENGTH_SHORT
                ).show()

                // Keep existing navigation behavior
                val intentProfile = Intent(context, ProfileActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intentProfile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RegisterDebug", "Erro ao criar utilizador: ${e.message}")
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.err_registration_api, e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    XploredTheme {
        RegistrationScreen()
    }
}
