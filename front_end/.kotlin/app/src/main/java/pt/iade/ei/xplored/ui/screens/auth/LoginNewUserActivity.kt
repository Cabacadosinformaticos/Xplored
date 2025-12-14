package pt.iade.ei.xplored.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.ui.screens.main.MainActivity
import pt.iade.ei.xplored.data.models.users.User
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.UserApiService
import pt.iade.ei.xplored.ui.theme.XploredTheme

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

    // UI State
    var isRegistering by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope() // We need this to launch the registration

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

                // Password
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
                            Icon(imageVector = image, contentDescription = null)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm password
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
                            Icon(imageVector = image, contentDescription = null)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Submit registration
                Button(
                    onClick = {
                        isRegistering = true
                        performRegistration(context, username, email, password, confirmPassword, scope) { success ->
                            isRegistering = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2835)),
                    enabled = !isRegistering
                ) {
                    if (isRegistering) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = stringResource(R.string.register_button), fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Link to Login
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
 * Validates input, CALLS API FIRST, and only saves locally if API succeeds.
 */
private fun performRegistration(
    context: Context,
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    scope: CoroutineScope,
    onComplete: (Boolean) -> Unit
) {
    val minLength = 3

    // 1. Basic Validation
    if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
        Toast.makeText(context, context.getString(R.string.err_fill_all_fields), Toast.LENGTH_SHORT).show()
        onComplete(false)
        return
    }

    if (username.length <= minLength || email.length <= minLength || password.length <= minLength) {
        Toast.makeText(context, context.getString(R.string.err_fields_too_short), Toast.LENGTH_LONG).show()
        onComplete(false)
        return
    }

    if (password != confirmPassword) {
        Toast.makeText(context, context.getString(R.string.err_passwords_do_not_match), Toast.LENGTH_LONG).show()
        onComplete(false)
        return
    }

    // 2. Local Duplicate Check (Fast Failure)
    val usersArray = SessionManager.getRegisteredUsersArray(context)
    for (i in 0 until usersArray.length()) {
        val u = usersArray.getJSONObject(i)
        if (u.optString("email").equals(email, ignoreCase = true)) {
            Toast.makeText(context, "Este email já está registado neste dispositivo.", Toast.LENGTH_LONG).show()
            onComplete(false)
            return
        }
    }

    // 3. API Call (Server Check)
    scope.launch {
        try {
            val api = ApiClient.instance.create(UserApiService::class.java)
            val newUserRequest = User(
                userId = null,
                name = username,
                email = email,
                about = "",
                points = 0,
                passwordHash = password
            )

            val createdUser = api.createUser(newUserRequest)

            withContext(Dispatchers.Main) {
                SessionManager.addRegisteredUser(context, username, email, password)
                SessionManager.setLoggedIn(context, true)
                SessionManager.saveUserData(context, createdUser.name, createdUser.email)
                SessionManager.saveUserPoints(context, 0)

                Toast.makeText(context, context.getString(R.string.msg_registration_success_welcome, createdUser.name), Toast.LENGTH_SHORT).show()

                val intent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intent)
                onComplete(true)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                // Provide specific feedback if possible, otherwise generic error
                val errorMsg = if (e.message?.contains("500") == true || e.message?.contains("400") == true) {
                    "Erro: O email ou utilizador já existe."
                } else {
                    "Erro de conexão: ${e.message}"
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
    }
}