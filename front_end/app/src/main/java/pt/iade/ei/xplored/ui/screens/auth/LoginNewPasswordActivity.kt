package pt.iade.ei.xplored.ui.screens.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONObject
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.PasswordResetConfirmRequest
import pt.iade.ei.xplored.network.UserApiService
import pt.iade.ei.xplored.ui.theme.XploredTheme
import retrofit2.HttpException

class LoginNewPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resetEmail = intent.getStringExtra("reset_email").orEmpty()
        val resetCode = intent.getStringExtra("reset_code").orEmpty()
        setContent {
            XploredTheme {
                NewPasswordScreen(resetEmail, resetCode)
            }
        }
    }
}

@Composable
fun NewPasswordScreen(resetEmail: String, resetCode: String) {
    // Local state for inputs and visibility toggles
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Back navigation
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

            // Screen content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(120.dp))

                // Title and subtitle
                Text(
                    text = stringResource(R.string.new_password_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.new_password_subtitle),
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp)
                )

                // New password input
                PasswordInputField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = stringResource(R.string.new_password_field),
                    isVisible = newPasswordVisible,
                    onVisibilityToggle = { newPasswordVisible = !newPasswordVisible }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm password input
                PasswordInputField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = stringResource(R.string.new_password_confirm_field),
                    isVisible = confirmPasswordVisible,
                    onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Triggers the real password reset on the backend
                Button(
                    onClick = {
                        if (newPassword.isBlank() || confirmPassword.isBlank()) {
                            Toast.makeText(context, context.getString(R.string.password_empty_fields), Toast.LENGTH_SHORT).show()
                        } else if (newPassword != confirmPassword) {
                            Toast.makeText(context, context.getString(R.string.password_mismatch), Toast.LENGTH_LONG).show()
                        } else if (newPassword.length < 6) {
                            Toast.makeText(context, context.getString(R.string.password_too_short), Toast.LENGTH_LONG).show()
                        } else {
                            isLoading = true
                            scope.launch {
                                try {
                                    val api = ApiClient.instance.create(UserApiService::class.java)
                                    api.confirmPasswordReset(
                                        PasswordResetConfirmRequest(
                                            email = resetEmail,
                                            code = resetCode,
                                            newPassword = newPassword
                                        )
                                    )

                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.password_reset_success),
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // Back to login and drop the reset screens from the back stack.
                                    val intent = Intent(context, LoginWelcomeBackActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    }
                                    context.startActivity(intent)
                                    (context as? ComponentActivity)?.finish()
                                } catch (e: HttpException) {
                                    val message = parseErrorMessage(e.response()?.errorBody()?.string())
                                        ?: context.getString(R.string.login_server_error, e.code())
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.login_error_network),
                                        Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                    enabled = newPassword.isNotBlank() && confirmPassword.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(text = stringResource(R.string.new_password_button), fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    // Password text field with show/hide toggle
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            val image = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (isVisible)
                stringResource(R.string.hide_password)
            else stringResource(R.string.show_password)

            IconButton(onClick = onVisibilityToggle) {
                Icon(imageVector = image, description)
            }
        }
    )
}

/** Reads the {error} field of a 400 response body, or null when it is not usable. */
private fun parseErrorMessage(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return try {
        JSONObject(body).optString("error").ifBlank { null }
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true)
@Composable
fun NewPasswordScreenPreview() {
    XploredTheme {
        NewPasswordScreen(resetEmail = "teste@email.com", resetCode = "123456")
    }
}
