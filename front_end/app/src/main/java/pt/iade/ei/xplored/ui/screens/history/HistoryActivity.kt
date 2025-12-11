package pt.iade.ei.xplored.ui.screens.history

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.CouponApiService
import pt.iade.ei.xplored.network.HistoryApiService
import pt.iade.ei.xplored.network.PointsHistoryResponse
import pt.iade.ei.xplored.network.RedeemedCouponResponse
import pt.iade.ei.xplored.ui.theme.XploredTheme
import pt.iade.ei.xplored.ui.theme.pointsAccent

// Same palette as the rest of the app.
private val PointsGreen = Color(0xFF2E7D32)
private val CodeBackground = Color(0xFFE0F2F1)
private val CodeGreen = Color(0xFF00796B)

// Extras used by CouponActivity to open the coupon tab directly.
const val EXTRA_HISTORY_TAB = "history_tab"
const val HISTORY_TAB_COUPONS = "coupons"

private enum class HistoryTab {
    Pontos, Cupoes
}

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startTab = if (intent.getStringExtra(EXTRA_HISTORY_TAB) == HISTORY_TAB_COUPONS) {
            HistoryTab.Cupoes
        } else {
            HistoryTab.Pontos
        }
        setContent {
            XploredTheme {
                HistoryScreen(startTab = startTab, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreen(startTab: HistoryTab, onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(startTab) }
    var points by remember { mutableStateOf<List<PointsHistoryResponse>>(emptyList()) }
    var coupons by remember { mutableStateOf<List<RedeemedCouponResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val email = SessionManager.getUserEmail(context)
        if (email.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val historyApi = ApiClient.instance.create(HistoryApiService::class.java)
            val couponApi = ApiClient.instance.create(CouponApiService::class.java)
            points = historyApi.getPointsHistory(email)
            coupons = couponApi.getRedeemedCoupons(email)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Não foi possível carregar o histórico.", Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pontos e cupões", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
        ) {
            val tabs = listOf(HistoryTab.Pontos, HistoryTab.Cupoes)
            TabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { positions ->
                    val idx = tabs.indexOf(selectedTab)
                    if (idx < positions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(positions[idx]),
                            color = Color(0xFFFF7A80),
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                when (tab) {
                                    HistoryTab.Pontos -> "Pontos"
                                    HistoryTab.Cupoes -> "Os meus cupões"
                                },
                                fontSize = 16.sp
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    HistoryTab.Pontos -> PointsTab(points)
                    HistoryTab.Cupoes -> CouponsTab(coupons)
                }
            }
        }
    }
}

@Composable
private fun PointsTab(entries: List<PointsHistoryResponse>) {
    if (entries.isEmpty()) {
        EmptyMessage("Ainda não tens movimentos de pontos.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(entries) { entry -> PointsRow(entry) }
    }
}

@Composable
private fun PointsRow(entry: PointsHistoryResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFCC66),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.reason ?: "Movimento de pontos",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val date = formatDate(entry.createdAt)
                if (date.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = if (entry.amount >= 0) "+${entry.amount}" else entry.amount.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (entry.amount >= 0) PointsGreen else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun CouponsTab(coupons: List<RedeemedCouponResponse>) {
    if (coupons.isEmpty()) {
        EmptyMessage("Ainda não resgataste cupões.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(coupons) { coupon -> RedeemedCouponCard(coupon) }
    }
}

@Composable
private fun RedeemedCouponCard(coupon: RedeemedCouponResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = pointsAccent(),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = coupon.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!coupon.merchantName.isNullOrBlank()) {
                        Text(coupon.merchantName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CodeBackground, RoundedCornerShape(8.dp))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = coupon.code ?: "Sem código",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = CodeGreen,
                    letterSpacing = 3.sp
                )
            }

            val date = formatDate(coupon.redeemedAt)
            if (date.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Resgatado em $date", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// "2026-10-09T14:03:22" -> "2026-10-09 14:03"
private fun formatDate(raw: String?): String =
    raw?.replace('T', ' ')?.take(16).orEmpty()

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HistoryScreenPreview() {
    XploredTheme {
        HistoryScreen(startTab = HistoryTab.Pontos, onBack = {})
    }
}
