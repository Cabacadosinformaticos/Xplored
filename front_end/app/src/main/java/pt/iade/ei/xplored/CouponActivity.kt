package pt.iade.ei.xplored

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset // Import for vertical adjustment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import pt.iade.ei.xplored.ui.theme.XploredTheme

// Custom colors based on the image
val GoldCouponColor = Color(0xFFFFCC66)
val YellowCouponColor = Color(0xFFFFFF99)
val DarkText = Color(0xFF1E2835)

// Sample data structure for the coupons
data class Coupon(
    val title: String,
    val description: String,
    val code: String,
    val merchant: String,
    val tagColor: Color,
    val details: String // Extended details shown in the popup
)

/**
 * Activity for the Coupons/Cupões screen.
 */
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

// --- Detail Dialog Composable (The Popup) ---

@Composable
fun CouponDetailDialog(coupon: Coupon, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        coupon.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DarkText
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Coupon Code (Highlighted)
                Text(
                    coupon.code,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = Color(0xFF00796B),
                    modifier = Modifier
                        .background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Merchant and Description
                Text(stringResource(R.string.coupon_from, coupon.merchant), fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))

                // Full Details
                Text(stringResource(R.string.offer_details_label), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleMedium, color = DarkText)
                Text(
                    coupon.details,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Action Button (Final Redeem)
                Button(
                    onClick = {
                        // TODO: Implement actual coupon activation/redemption logic
                        onClose()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkText)
                ) {
                    Text(stringResource(R.string.activate_now), fontSize = 18.sp)
                }
            }
        }
    }
}


/**
 * Composable for the Loyalty Points Progress Bar.
 */
@Composable
fun PointsProgressBar(currentPoints: Int, maxPoints: Int = 1000) {
    val stages = listOf(0, 250, 500, 750, 1000)
    val colorPrimary = MaterialTheme.colorScheme.primary // Using primary color for active bar

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Calculate the progress percentage for the bar
        val progress = (currentPoints.toFloat() / maxPoints.toFloat()).coerceIn(0f, 1f)

        // Draw the progress bar track and filling
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            val trackColor = Color.LightGray.copy(alpha = 0.5f)
            val fillWidth = size.width * progress

            // Draw the full track
            drawLine(
                color = trackColor,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = size.height
            )

            // Draw the filled progress
            drawLine(
                color = colorPrimary,
                start = Offset(0f, size.height / 2),
                end = Offset(fillWidth, size.height / 2),
                strokeWidth = size.height
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stage Markers (0, 250, 500, 750, 1000)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stages.forEach { stage ->
                val isActive = stage <= currentPoints

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        // Stage Circle
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawCircle(
                                color = if (isActive) colorPrimary else Color.LightGray,
                                radius = size.minDimension / 2
                            )
                        }
                        // Checkmark icon only appears if the stage is reached (isActive is true)
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

/**
 * Composable for rendering a single Coupon Card.
 */
@Composable
fun CouponCard(coupon: Coupon, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Ensures the height adapts to the tallest content
            .padding(bottom = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Vertical Tag
            Box(
                modifier = Modifier
                    .width(40.dp) // Fixed width for the tag area
                    .fillMaxHeight() // Fill the height determined by the Row/IntrinsicSize.Min
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(coupon.tagColor),
                contentAlignment = Alignment.Center
            ) {
                // Rotated Text to mimic the vertical tag
                //              Text(
                //                  text = coupon.merchant,
                //                  color = DarkText,
                //                  fontSize = 14.sp,
                //                  fontWeight = FontWeight.Bold,
                //                  textAlign = TextAlign.Center,
                //                  modifier = Modifier
                //                      .rotate(-90f)
                //                      .fillMaxHeight()
                //                      .padding(vertical = 4.dp)
                //              )
            }

            // Right Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title/Discount
                Text(
                    text = coupon.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )

                // Coupon Code
                Text(
                    text = coupon.code,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkText,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Description/Details
                Text(
                    text = coupon.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Terms and Conditions Link
                Text(
                    text = stringResource(R.string.terms_and_conditions),
                    fontSize = 12.sp,
                    color = DarkText,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { /* TODO: Show terms modal/navigate */ }
                        .padding(bottom = 12.dp)
                )
            }
        }
    }
}


@Composable
fun CouponScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val currentPoints = SessionManager.getUserPoints(context)
    // Hardcoded value from the image

    // State to hold the currently selected coupon for the dialog
    var selectedCoupon by remember { mutableStateOf<Coupon?>(null) }

    // Sample data list (Updated with more detailed 'details' for the popup)
    val coupons = listOf(
        Coupon(
            title = "Oferta 2 por 1*",
            description = "Na compra de uma caneca de cerveja, recebe outra totalmente grátis.",
            code = "MIMOSO2EM1",
            merchant = "Restaurante Mimoso", // Long name check
            tagColor = GoldCouponColor,
            details = "Válido apenas para bebidas de pressão de marca branca. Não acumulável com outras promoções. Oferta válida de Segunda a Quinta-feira, das 17h às 20h. Apresente o código MIMOSO2EM1 no momento do pedido."
        ),
        Coupon(
            title = "15% de desconto num menu*",
            description = "Save $25 on all transactions.",
            code = "KEBAB15",
            merchant = "Kebab Express", // Medium name check
            tagColor = YellowCouponColor,
            details = "Desconto de 15% aplicável a qualquer Menu Kebab ou Menu Prato. Exclui bebidas e extras. Válido em qualquer dia da semana. Limite de uma utilização por cliente."
        ),
        Coupon(
            title = "5% de desconto em produtos regionais*",
            description = "Poupa 5% em vinhos, queijos e enchidos de origem portuguesa.",
            code = "MARIO5",
            merchant = "Mercearia do Mário", // Another long name check
            tagColor = GoldCouponColor,
            details = "Válido para todas as compras de produtos com a etiqueta 'Regional'. Mínimo de compra de 20€ exigido. Expira no final do mês. Não se aplica a pão fresco ou pastelaria."
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(top = 80.dp), // Pushed down to clear the back button
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Screen Title
                Text(
                    text = stringResource(R.string.coupon_store_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Start)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPoints.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkText
                    )
                    // FIX: Added a negative vertical offset to move the star up for better alignment.
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            //.padding(start = 4.dp)
                            .offset(y = (-8).dp), // Nudges the star up by 4dp
                        contentAlignment = Alignment.Center
                    ) {
                        Text("★", fontSize = 36.sp, color = GoldCouponColor)
                    }
                }

                // Progress Bar
                PointsProgressBar(currentPoints = currentPoints)

                Spacer(modifier = Modifier.height(32.dp))

                // List of Coupons
                coupons.forEach { coupon ->
                    CouponCard(
                        coupon = coupon,
                        onClick = { selectedCoupon = coupon } // Set state to show dialog
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            IconButton(
                onClick = { (context as? ComponentActivity)?.finish() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 26.dp, start = 8.dp)
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

    // --- Coupon Detail Dialog Rendering ---
    // If a coupon is selected, show the dialog
    selectedCoupon?.let { coupon ->
        CouponDetailDialog(coupon = coupon) {
            // Dismiss the dialog when onClose is called
            selectedCoupon = null
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CouponScreenPreview() {
    XploredTheme {
        CouponScreen()
    }
}
