package pt.iade.ei.xplored

import android.content.Context
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
import pt.iade.ei.xplored.ui.theme.XploredTheme

val PrimaryDark = Color(0xFF1E2835)
val SecondaryLight = Color(0xFFE0E0E0)
val HighlightedColor = Color(0xFF35C2C1)

class IdentityCheckActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XploredTheme {
                IdentityCheckScreen()
            }
        }
    }
}

@Composable
fun OtpBox(text: String, isFocused: Boolean) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .height(160.dp)
            .border(
                width = 2.dp,
                color = if (isFocused) HighlightedColor else SecondaryLight,
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDark
        )
    }
}

@Composable
fun IdentityCheckScreen() {
    val context = LocalContext.current
    val otpLength = 4
    val correctCode = "1234"

    var otpValue by remember { mutableStateOf(TextFieldValue(text = "", selection = TextRange(0))) }

    val verifyOtp: () -> Unit = {
        if (otpValue.text.length == otpLength) {
            if (otpValue.text == correctCode) {
                Toast.makeText(context, context.getString(R.string.verification_success), Toast.LENGTH_SHORT).show()
                val intent = Intent(context, LoginNewPasswordActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(intent)
                (context as? ComponentActivity)?.finish()
            } else {
                Toast.makeText(context, context.getString(R.string.incorrect_code), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, context.getString(R.string.incomplete_code), Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
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
                    tint = Color.Black
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
                    color = Color.Gray,
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
                        .height(180.dp)
                )

                Button(
                    onClick = verifyOtp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
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
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.resend_button),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, context.getString(R.string.code_resent), Toast.LENGTH_SHORT).show()
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
        IdentityCheckScreen()
    }
}
