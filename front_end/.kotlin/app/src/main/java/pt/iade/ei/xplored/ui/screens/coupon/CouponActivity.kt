package pt.iade.ei.xplored.ui.screens.coupon

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.CouponApiService
import pt.iade.ei.xplored.network.CouponResponse
import pt.iade.ei.xplored.network.RedemptionRequest
import pt.iade.ei.xplored.ui.theme.XploredTheme

// Custom colors
val GoldCouponColor = Color(0xFFFFCC66)
val YellowCouponColor = Color(0xFFFFFF99)
val DarkText = Color(0xFF1E2835)

// Extension to assign colors based on cost
fun CouponResponse.getColor(): Color {
    return if (this.cost >= 300) GoldCouponColor else YellowCouponColor
}

class CouponActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XploredTheme {
                CouponScreen()
            }
        }
    }
}

@Composable
fun TermsAndConditionsDialog(terms: String, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                text = stringResource(R.string.terms_and_conditions),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = terms.ifBlank { stringResource(R.string.no_additional_terms) },
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onClose,
                colors = ButtonDefaults.textButtonColors(contentColor = DarkText)
            ) {
                Text(stringResource(R.string.close))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CouponDetailDialog(
    coupon: CouponResponse,
    userPoints: Int,
    onClose: () -> Unit,
    onRedeemSuccess: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    // NEW: Track if the user has bought the coupon
    var isRedeemed by remember { mutableStateOf(false) }

    val canAfford = userPoints >= coupon.cost

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        coupon.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DarkText,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // --- CODE DISPLAY LOGIC ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isRedeemed) Color(0xFFE0F2F1) else Color.LightGray.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRedeemed) {
                        // Show REAL Code
                        Text(
                            coupon.code,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp,
                            color = Color(0xFF00796B)
                        )
                    } else {
                        // Show Mask
                        Text(
                            "•••• - ••••",
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.Gray,
                            letterSpacing = 4.sp
                        )
                    }
                }

                if (isRedeemed) {
                    Text(
                        "Cupão Pronto a Usar!",
                        color = Color(0xFF00796B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(stringResource(R.string.coupon_from, coupon.merchant), fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))

                Text(stringResource(R.string.offer_details_label), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleMedium, color = DarkText)
                Text(
                    coupon.details ?: stringResource(R.string.no_additional_details),
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(32.dp))

                // BUTTON LOGIC
                if (!isRedeemed) {
                    // Show "Activate" Button
                    Button(
                        onClick = {
                            if (!isProcessing && canAfford) {
                                isProcessing = true
                                scope.launch {
                                    try {
                                        val email = SessionManager.getUserEmail(context)
                                        val api = ApiClient.instance.create(CouponApiService::class.java)
                                        val request = RedemptionRequest(email, coupon.id)
                                        val response = api.redeemCoupon(request)

                                        if (response.isSuccessful && response.body() != null) {
                                            val newBalance = response.body()!!.newBalance
                                            Toast.makeText(context, "Cupão ativado com sucesso!", Toast.LENGTH_SHORT).show()

                                            // Update Parent Points
                                            onRedeemSuccess(newBalance)

                                            // REVEAL CODE locally
                                            isRedeemed = true
                                            isProcessing = false
                                        } else {
                                            val errorMsg = response.errorBody()?.string() ?: "Erro desconhecido"
                                            Toast.makeText(context, "Erro: $errorMsg", Toast.LENGTH_LONG).show()
                                            isProcessing = false
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "Erro de rede.", Toast.LENGTH_SHORT).show()
                                        isProcessing = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (canAfford) DarkText else Color.Gray),
                        enabled = canAfford && !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                if (canAfford) "ATIVAR POR ${coupon.cost} PONTOS" else "PONTOS INSUFICIENTES (${coupon.cost})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Show "Close" Button (since they already bought it)
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Fechar")
                    }
                }
            }
        }
    }
}

@Composable
fun PointsProgressBar(currentPoints: Int, maxPoints: Int = 1000) {
    val stages = listOf(0, 250, 500, 750, 1000)
    val colorPrimary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val progress = (currentPoints.toFloat() / maxPoints.toFloat()).coerceIn(0f, 1f)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            val trackColor = Color.LightGray.copy(alpha = 0.5f)
            val fillWidth = size.width * progress

            drawLine(
                color = trackColor,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = size.height
            )

            drawLine(
                color = colorPrimary,
                start = Offset(0f, size.height / 2),
                end = Offset(fillWidth, size.height / 2),
                strokeWidth = size.height
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stages.forEach { stage ->
                val isActive = stage <= currentPoints
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawCircle(
                                color = if (isActive) colorPrimary else Color.LightGray,
                                radius = size.minDimension / 2
                            )
                        }
                        if (isActive) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = stage.toString(),
                        fontSize = 12.sp,
                        color = if (isActive) Color.Black else Color.Gray,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CouponCard(
    coupon: CouponResponse,
    onClick: () -> Unit,
    onTermsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Keeps height consistent
            .padding(bottom = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Color Strip
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(coupon.getColor())
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = coupon.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // --- FIX: Mask the code in the list ---
                    Text(
                        text = "•••• - ••••",
                        fontSize = 24.sp, // Slightly smaller to fit mask
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.LightGray, // Gray implies "locked"
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    // --------------------------------------

                    Spacer(modifier = Modifier.weight(1f))

                    // Cost
                    Text(
                        text = stringResource(R.string.points_label, coupon.cost),
                        fontSize = 14.sp,
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = coupon.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.terms_and_conditions),
                    fontSize = 12.sp,
                    color = DarkText,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .clickable { onTermsClick() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // State
    // We maintain points locally to update UI instantly without needing full profile refresh
    var currentPoints by remember { mutableIntStateOf(SessionManager.getUserPoints(context)) }

    var selectedCoupon by remember { mutableStateOf<CouponResponse?>(null) }
    var termsCoupon by remember { mutableStateOf<CouponResponse?>(null) }
    var coupons by remember { mutableStateOf<List<CouponResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch from Backend
    LaunchedEffect(Unit) {
        try {
            val api = ApiClient.instance.create(CouponApiService::class.java)
            coupons = api.getActiveCoupons()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, context.getString(R.string.error_loading_coupons), Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.coupon_store_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Points Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPoints.toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkText
                )
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .offset(y = (-8).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("★", fontSize = 36.sp, color = GoldCouponColor)
                }
            }

            PointsProgressBar(currentPoints = currentPoints)

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (coupons.isEmpty()) {
                Text(stringResource(R.string.no_coupons_available), modifier = Modifier.align(Alignment.CenterHorizontally), color = Color.Gray)
            } else {
                coupons.forEach { coupon ->
                    CouponCard(
                        coupon = coupon,
                        onClick = { selectedCoupon = coupon },
                        onTermsClick = { termsCoupon = coupon }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    selectedCoupon?.let { coupon ->
        CouponDetailDialog(
            coupon = coupon,
            userPoints = currentPoints,
            onClose = { selectedCoupon = null },
            onRedeemSuccess = { newBalance ->
                currentPoints = newBalance
                SessionManager.saveUserPoints(context, newBalance) // Persist locally
            }
        )
    }

    termsCoupon?.let { coupon ->
        TermsAndConditionsDialog(terms = coupon.details ?: "") { termsCoupon = null }
    }
}

@Preview(showBackground = true)
@Composable
fun CouponScreenPreview() {
    XploredTheme {
        CouponScreen()
    }
}