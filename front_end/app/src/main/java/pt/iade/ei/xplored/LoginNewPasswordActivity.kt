package pt.iade.ei.xplored

import android.content.Context
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
import pt.iade.ei.xplored.ui.theme.XploredTheme
import androidx.core.content.edit

class LoginNewPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XploredTheme {
                NewPasswordScreen()
            }
        }
    }
}

@Composable
fun NewPasswordScreen() {
    // Local state for inputs and visibility toggles
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

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

                // Triggers password update flow
                Button(
                    onClick = { performPasswordReset(context, newPassword, confirmPassword) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                    enabled = newPassword.isNotBlank() && confirmPassword.isNotBlank()
                ) {
                    Text(text = stringResource(R.string.new_password_button), fontSize = 18.sp)
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

private fun performPasswordReset(context: Context, newPass: String, confirmPass: String) {
    // Basic validation
    if (newPass.isBlank() || confirmPass.isBlank()) {
        Toast.makeText(context, context.getString(R.string.password_empty_fields), Toast.LENGTH_SHORT).show()
        return
    }

    if (newPass != confirmPass) {
        Toast.makeText(context, context.getString(R.string.password_mismatch), Toast.LENGTH_LONG).show()
        return
    }

    // Determine current user (email is used as key)
    val email = SessionManager.getUserEmail(context)
    if (email.isNullOrBlank()) {
        Toast.makeText(context, context.getString(R.string.err_no_active_account), Toast.LENGTH_LONG).show()
        return
    }

    // Load users and update matching record
    val usersArray = SessionManager.getRegisteredUsersArray(context)
    var userFound = false
    for (i in 0 until usersArray.length()) {
        val userObj = usersArray.getJSONObject(i)
        if (userObj.optString("email").equals(email, ignoreCase = true)) {
            userObj.put("password", newPass)
            userFound = true
            break
        }
    }

    if (!userFound) {
        Toast.makeText(context, context.getString(R.string.err_account_not_found), Toast.LENGTH_LONG).show()
        return
    }

    // Persist updated list (mirrors SessionManager storage keys)
    // PREF_USERS = "XploredUsers", KEY_USERS_LIST = "usersList"
    context
        .getSharedPreferences("XploredUsers", Context.MODE_PRIVATE)
        .edit {
            putString("usersList", usersArray.toString())
        }

    Toast.makeText(context, context.getString(R.string.password_reset_success), Toast.LENGTH_LONG).show()

    // Navigate back to welcome/login and clear back stack
    val intent = Intent(context, LoginWelcomeBackActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    context.startActivity(intent)
    (context as? ComponentActivity)?.finish()
}

@Preview(showBackground = true)
@Composable
fun NewPasswordScreenPreview() {
    XploredTheme {
        NewPasswordScreen()
    }
}
