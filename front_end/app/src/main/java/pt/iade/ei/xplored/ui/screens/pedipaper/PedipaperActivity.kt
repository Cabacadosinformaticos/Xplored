package pt.iade.ei.xplored.ui.screens.pedipaper

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.Pedipaper
import pt.iade.ei.xplored.network.PedipaperApiService
import pt.iade.ei.xplored.ui.theme.XploredTheme

class PedipaperActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XploredTheme {
                PedipaperListScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedipaperListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var routes by remember { mutableStateOf<List<Pedipaper>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch Routes from Backend
    LaunchedEffect(Unit) {
        try {
            val api = ApiClient.instance.create(PedipaperApiService::class.java)
            routes = api.getAllPedipapers()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro a carregar rotas", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Desafios Xplored") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (routes.isEmpty()) {
                Text("Sem rotas disponíveis.", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(routes) { route ->
                        PedipaperCard(route) {
                            // On Click: JOIN and Start Game
                            scope.launch {
                                try {
                                    val email = SessionManager.getUserEmail(context)
                                    val api = ApiClient.instance.create(PedipaperApiService::class.java)

                                    // 1. Tell backend we are joining
                                    api.joinPedipaper(route.pediId, email)

                                    // 2. Open Game Screen
                                    val intent = Intent(context, RouteGameActivity::class.java)
                                    intent.putExtra("PEDI_ID", route.pediId)
                                    intent.putExtra("PEDI_NAME", route.name)
                                    // NEW: Pass points so we can show them on victory
                                    intent.putExtra("PEDI_POINTS", route.totalPoints)
                                    context.startActivity(intent)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Erro ao iniciar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PedipaperCard(route: Pedipaper, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0F2F1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsWalk, null, tint = Color(0xFF00796B))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(route.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(route.description ?: "", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${route.totalPoints} pts", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            }
        }
    }
}