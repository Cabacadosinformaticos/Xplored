package pt.iade.ei.xplored.ui.screens.pedipaper

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.PedipaperApiService
import pt.iade.ei.xplored.network.PhotoUploadClient
import pt.iade.ei.xplored.network.StopResponse
import pt.iade.ei.xplored.network.UserApiService // IMPORT ADDED
import pt.iade.ei.xplored.ui.theme.XploredTheme

class RouteGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pediId = intent.getLongExtra("PEDI_ID", 0)
        val pediName = intent.getStringExtra("PEDI_NAME") ?: "Rota"
        val pediPoints = intent.getIntExtra("PEDI_POINTS", 0) // NEW: Get total points

        setContent {
            XploredTheme {
                RouteGameScreen(pediId, pediName, pediPoints, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteGameScreen(pediId: Long, pediName: String, totalPoints: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Game State
    var stops by remember { mutableStateOf<List<StopResponse>>(emptyList()) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Location State
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var distanceToTarget by remember { mutableFloatStateOf(Float.MAX_VALUE) }

    // Map State
    val lisbon = LatLng(38.7223, -9.1393)
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(lisbon, 15f) }

    // --- 1. SETUP LOCATION ENGINE ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    userLocation = LatLng(location.latitude, location.longitude)

                    // Calculate distance to current target
                    if (stops.isNotEmpty() && currentStepIndex < stops.size) {
                        val target = stops[currentStepIndex]
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            location.latitude, location.longitude,
                            target.lat, target.lng,
                            results
                        )
                        distanceToTarget = results[0]
                    }
                }
            }
        }
    }

    // Request Permissions & Start Updates
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .build()

            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        }
    }

    // --- 2. LOAD ROUTE DATA ---
    LaunchedEffect(pediId) {
        try {
            val api = ApiClient.instance.create(PedipaperApiService::class.java)
            stops = api.getStops(pediId)

            if (stops.isNotEmpty()) {
                val first = stops[0]
                cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(first.lat, first.lng), 16f)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao carregar rota", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // --- 3. HELPER: FINISH ROUTE & SYNC POINTS ---
    fun finishRoute(email: String) {
        scope.launch {
            try {
                // 1. Tell backend we finished (Backend adds points to DB)
                val api = ApiClient.instance.create(PedipaperApiService::class.java)
                api.completePedipaper(pediId, email)

                // 2. Fetch updated user profile to get new points balance immediately
                val userApi = ApiClient.instance.create(UserApiService::class.java)
                val updatedUser = userApi.getUserByEmail(email)

                // 3. Update Local Session
                SessionManager.saveUserPoints(context, updatedUser.points)

                isFinished = true
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao finalizar: ${e.message}", Toast.LENGTH_SHORT).show()
                // Force finish UI anyway so user isn't stuck
                isFinished = true
            }
        }
    }

    // --- 4. PHOTO UPLOAD LOGIC ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && stops.isNotEmpty()) {
            scope.launch {
                val currentStop = stops[currentStepIndex]
                val email = SessionManager.getUserEmail(context)

                Toast.makeText(context, "A enviar foto...", Toast.LENGTH_SHORT).show()

                // Upload Evidence
                PhotoUploadClient.uploadPhoto(
                    context, uri, email,
                    placeId = currentStop.placeId.toString(),
                    reviewId = "STOP-EVIDENCE-${currentStop.stopId}"
                )

                // Advance Game
                if (currentStepIndex < stops.size - 1) {
                    currentStepIndex++
                    Toast.makeText(context, "Boa! Próxima paragem desbloqueada.", Toast.LENGTH_SHORT).show()
                    val next = stops[currentStepIndex]
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(next.lat, next.lng), 16f))
                } else {
                    finishRoute(email)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pediName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (isFinished) {
                // --- VICTORY SCREEN ---
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(120.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Missão Cumprida!", fontSize = 32.sp, fontWeight = FontWeight.Bold)

                    // Show actual points won
                    Text("Ganhaste +$totalPoints pontos!", color = Color(0xFFE65100), fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2835))
                    ) {
                        Text("Voltar ao Menu")
                    }
                }
            } else if (!isLoading && stops.isNotEmpty() && currentStepIndex < stops.size) {
                // --- GAME SCREEN ---
                val currentStop = stops[currentStepIndex]
                val isNearby = distanceToTarget <= 50

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true)
                ) {
                    Marker(
                        state = MarkerState(position = LatLng(currentStop.lat, currentStop.lng)),
                        title = currentStop.placeName,
                        snippet = "Destino Atual"
                    )
                }

                // --- BOTTOM CARD ---
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE3F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${currentStepIndex + 1}", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Próxima Paragem", fontSize = 12.sp, color = Color.Gray)
                                Text(currentStop.placeName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(currentStop.taskDescription ?: "Vai até ao local.", color = Color(0xFF424242))

                        Spacer(Modifier.height(8.dp))
                        if (!isNearby) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Estás a ${distanceToTarget.toInt()}m. Aproxima-te (<50m).",
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Estás no local!", color = Color.Green, fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        val buttonText = if (currentStop.requiresPhoto) "Tirar Foto & Continuar" else "Fazer Check-in"
                        val buttonIcon = if (currentStop.requiresPhoto) Icons.Default.CameraAlt else Icons.Default.CheckCircle

                        Button(
                            onClick = {
                                if (currentStop.requiresPhoto) {
                                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                } else {
                                    // Simple Check-in
                                    scope.launch {
                                        if (currentStepIndex < stops.size - 1) {
                                            currentStepIndex++
                                            Toast.makeText(context, "Check-in feito!", Toast.LENGTH_SHORT).show()
                                            val next = stops[currentStepIndex]
                                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(next.lat, next.lng), 16f))
                                        } else {
                                            val email = SessionManager.getUserEmail(context)
                                            finishRoute(email)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = isNearby,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNearby) Color(0xFF1E2835) else Color.LightGray
                            )
                        ) {
                            Icon(buttonIcon, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isNearby) buttonText else "Aproxima-te para desbloquear")
                        }
                    }
                }
            } else if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}