package pt.iade.ei.xplored.ui.screens.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.UserApiService
import pt.iade.ei.xplored.ui.screens.main.MainActivity
import pt.iade.ei.xplored.ui.theme.XploredTheme
import retrofit2.HttpException

class LoginWelcomeBackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XploredTheme {
                LoginWelcomeBackScreen(
                    onLoginSuccess = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onNavigateRegister = {
                        startActivity(Intent(this, LoginNewUserActivity::class.java))
                    },
                    onNavigateForgot = {
                        startActivity(Intent(this, LoginForgotPasswordActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun LoginWelcomeBackScreen(
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit,
    onNavigateForgot: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.login_welcome_back_line1), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.login_welcome_back_line2), fontSize = 18.sp)
        Text(stringResource(R.string.login_welcome_back_line3), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.login_email)) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.login_password)) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = stringResource(R.string.cd_toggle_password))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.login_forgot_password),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.End).clickable(onClick = onNavigateForgot)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    scope.launch {
                        try {
                            val api = ApiClient.instance.create(UserApiService::class.java)
                            // Call the Backend Endpoint
                            val user = api.getUserByEmailAndPassword(email, password)

                            if (user != null) {
                                // 1. Save Session Locally
                                SessionManager.setLoggedIn(context, true)
                                SessionManager.saveUserData(context, user.name, user.email)
                                SessionManager.saveUserPoints(context, user.points)
                                SessionManager.saveUserAbout(context, user.about)
                                SessionManager.saveUserCountry(context, user.country ?: "")

                                // 2. Navigate
                                Toast.makeText(context, "Bem-vindo, ${user.name}!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, "Credenciais inválidas.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: HttpException) {
                            if (e.code() == 401 || e.code() == 404) {
                                Toast.makeText(context, "Email ou password incorretos.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Erro no servidor: ${e.code()}", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text(stringResource(R.string.login_button))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.login_no_account))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                stringResource(R.string.register_now),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onNavigateRegister)
            )
        }
    }
}