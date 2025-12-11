package pt.iade.ei.xplored.ui.screens.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.PasswordResetRequest
import pt.iade.ei.xplored.network.UserApiService
import pt.iade.ei.xplored.ui.theme.XploredTheme

val HighlightedColor = Color(0xFF35C2C1)

class IdentityCheckActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val resetEmail = intent.getStringExtra("reset_email").orEmpty()
        setContent {
            XploredTheme {
                IdentityCheckScreen(resetEmail)
            }
        }
    }
}

@Composable
fun OtpBox(text: String, isFocused: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = if (isFocused) HighlightedColor else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun IdentityCheckScreen(resetEmail: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val otpLength = 6

    var otpValue by remember { mutableStateOf(TextFieldValue(text = "", selection = TextRange(0))) }

    // The code is checked by the backend on the confirm step; here we only collect it.
    val verifyOtp: () -> Unit = {
        if (otpValue.text.length == otpLength) {
            val intent = Intent(context, LoginNewPasswordActivity::class.java).apply {
                putExtra("reset_email", resetEmail)
                putExtra("reset_code", otpValue.text)
            }
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        } else {
            Toast.makeText(context, context.getString(R.string.incomplete_code), Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

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
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(120.dp))

                Text(
                    text = stringResource(R.string.otp_verification_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.otp_verification_subtitle),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )

                BasicTextField(
                    value = otpValue,
                    onValueChange = { newValue ->
                        if (newValue.text.length <= otpLength && newValue.text.all { it.isDigit() }) {
                            otpValue = newValue.copy(selection = TextRange(newValue.text.length))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    decorationBox = {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 48.dp)
                        ) {
                            repeat(otpLength) { index ->
                                val char = otpValue.text.getOrNull(index)?.toString() ?: ""
                                OtpBox(
                                    text = char,
                                    isFocused = index == otpValue.text.length && index < otpLength
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Button(
                    onClick = verifyOtp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = otpValue.text.length == otpLength
                ) {
                    Text(stringResource(R.string.verify_button), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.padding(bottom = 32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.didnt_receive_code),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.resend_button),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            // Ask the backend for a fresh 6 digit code for the same email.
                            if (resetEmail.isBlank()) {
                                Toast.makeText(context, context.getString(R.string.code_resend_error), Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    try {
                                        ApiClient.instance.create(UserApiService::class.java)
                                            .requestPasswordReset(PasswordResetRequest(resetEmail))
                                        Toast.makeText(context, context.getString(R.string.code_resent), Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, context.getString(R.string.code_resend_error), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IdentityCheckScreenPreview() {
    XploredTheme {
        IdentityCheckScreen(resetEmail = "teste@email.com")
    }
}
