package pt.iade.ei.xplored.ui.screens.main

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.gson.JsonParser
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.data.models.photos.PhotoItem
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.PhotoStatus
import pt.iade.ei.xplored.data.models.places.Place
import pt.iade.ei.xplored.data.models.reviews.Review
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.FavoriteApiService
import pt.iade.ei.xplored.network.FavoriteRequest
import pt.iade.ei.xplored.network.PhotoApiService
import pt.iade.ei.xplored.network.PhotoUploadClient
import pt.iade.ei.xplored.network.PlaceApiService
import pt.iade.ei.xplored.network.PlaceRequest
import pt.iade.ei.xplored.network.ReactionRequest
import pt.iade.ei.xplored.network.ReviewApiService
import pt.iade.ei.xplored.network.VisitApiService
import pt.iade.ei.xplored.network.VisitRequest
import pt.iade.ei.xplored.repositories.PhotoRepository
import pt.iade.ei.xplored.repositories.PlaceRepository
import pt.iade.ei.xplored.ui.components.InitialsAvatar
import pt.iade.ei.xplored.ui.screens.auth.LoginWelcomeBackActivity
import pt.iade.ei.xplored.ui.screens.coupon.CouponActivity
import pt.iade.ei.xplored.ui.screens.pedipaper.PedipaperActivity
import pt.iade.ei.xplored.ui.screens.place.NewPlaceActivity
import pt.iade.ei.xplored.ui.screens.profile.ProfileActivity
import pt.iade.ei.xplored.ui.screens.reviews.NewReviewActivity
import pt.iade.ei.xplored.ui.theme.CategoryBlue
import pt.iade.ei.xplored.ui.theme.CategoryGreen
import pt.iade.ei.xplored.ui.theme.CategoryMagenta
import pt.iade.ei.xplored.ui.theme.CategoryOrange
import pt.iade.ei.xplored.ui.theme.CategoryYellow
import pt.iade.ei.xplored.ui.theme.XploredTheme
import pt.iade.ei.xplored.ui.theme.mapStyleResource
import java.io.IOException
import java.text.Normalizer
import java.util.Locale
import kotlin.math.roundToInt

// Extras used by FavoritesActivity to open the map on a saved place.
const val EXTRA_FOCUS_PLACE_ID = "focus_place_id"
const val EXTRA_FOCUS_LAT = "focus_lat"
const val EXTRA_FOCUS_LNG = "focus_lng"

// Radius (in metres) within which a check-in at a place counts.
private const val CHECKIN_RADIUS_METERS = 100f

// Case and accent insensitive key used by the place search (pt-PT names).
private fun normalizeForSearch(text: String): String {
    val lower = Normalizer.normalize(text.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
    return lower.replace("\\p{Mn}+".toRegex(), "")
}

// Reads the pt-PT "error" field from a server error body, or null.
private fun parseServerError(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        JsonParser.parseString(raw).asJsonObject.get("error")?.asString
    } catch (e: Exception) {
        null
    }
}

data class CategoryButtonInfo(
    val text: String,
    val icon: ImageVector,
    val contentDescription: String,
    val color: Color,
    // Text and icon colour that stays readable on the category chip.
    val contentColor: Color
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            XploredTheme {
                MainView()
            }
        }
    }
}

// --- HELPER FUNCTION FOR DISTANCE ---
fun calculateDistanceKm(start: LatLng, end: LatLng): Float {
    val results = FloatArray(1)
    Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results)
    return results[0] / 1000f // Convert meters to km
}

@Composable
fun ExpandedActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        // The whole row is the tap target, not just the round icon.
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(end = 8.dp)
        )
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(50)
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    }
}

// Small orange chip shown while the user's own place waits for moderation.
@Composable
private fun PendingBadge() {
    Box(
        modifier = Modifier
            .background(Color(0xFFFFF3CD), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.place_pending_badge),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8A6D3B)
        )
    }
}

// Compact icon + label action used in the place detail sheet.
@Composable
private fun PlaceActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true
) {
    val alpha = if (enabled) 1f else 0.35f
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = tint.copy(alpha = alpha), modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
    }
}

private suspend fun getAddressFromLatLng(context: Context, latLng: LatLng): String {
    if (!Geocoder.isPresent()) {
        return context.getString(R.string.geocoder_not_available)
    }
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        }
        if (addresses != null && addresses.isNotEmpty()) {
            addresses[0].getAddressLine(0) ?: context.getString(R.string.address_not_found)
        } else {
            context.getString(R.string.address_not_found)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        context.getString(R.string.address_could_not_retrieve)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView() {
    val context = LocalContext.current

    // Fallback centre: the seeded places around Parque das Nações, never (0,0).
    val defaultPosition = LatLng(38.7680, -9.0960)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 14f)
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    // Start from the real permission state, otherwise the map crashes on first launch
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isActionMenuExpanded by remember { mutableStateOf(false) }
    // True once the camera moved to the user (or fell back to the default centre).
    var centeredOnUser by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }

    // --- MAIN PLACE LIST STATE ---
    // Initialize with local data, then refresh from server
    var places by remember { mutableStateOf(PlaceRepository.getPlaces().toList()) }
    val scope = rememberCoroutineScope()

    // --- REFRESH FUNCTION ---
    // This updates the main list from the server (getting new stars/ratings)
    fun refreshPlaces() {
        scope.launch {
            val serverPlaces = PlaceRepository.fetchPlacesFromBackend(context)
            if (serverPlaces.isNotEmpty()) {
                places = serverPlaces
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshPlaces()
    }

    // --- DELETE PLACE ---
    // Only the author can delete; the server is the source of truth.
    fun deletePlace(place: Place) {
        val id = place.id.toLongOrNull() ?: return
        val email = SessionManager.getUserEmail(context)
        scope.launch {
            try {
                val api = ApiClient.instance.create(PlaceApiService::class.java)
                val response = api.deletePlace(id, email)
                if (response.isSuccessful) {
                    PlaceRepository.removePlace(context, place.id)
                    places = PlaceRepository.getPlaces()
                    Toast.makeText(context, "Local apagado.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Não foi possível apagar o local.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erro de rede ao apagar o local.", Toast.LENGTH_LONG).show()
            }
        }
    }

    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var fullScreenImageUri by remember { mutableStateOf<Uri?>(null) }

    // --- FILTER STATE ---
    var radiusKm by remember { mutableFloatStateOf(50f) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // --- FOCUS REQUEST ---
    // FavoritesActivity can ask the map to open on a saved place.
    val activity = context as? Activity
    val focusPlaceId = remember { activity?.intent?.getStringExtra(EXTRA_FOCUS_PLACE_ID) }
    val focusLat = remember { activity?.intent?.getDoubleExtra(EXTRA_FOCUS_LAT, Double.NaN) }
    val focusLng = remember { activity?.intent?.getDoubleExtra(EXTRA_FOCUS_LNG, Double.NaN) }

    LaunchedEffect(places, focusPlaceId) {
        if (places.isEmpty()) return@LaunchedEffect
        val target = focusPlaceId?.let { id -> places.firstOrNull { it.id == id } }
        if (target != null) {
            selectedPlace = target
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target.latLng, 16f))
        } else if (focusLat != null && focusLng != null && !focusLat.isNaN() && !focusLng.isNaN()) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(focusLat, focusLng), 16f))
        }
    }

    // --- UPDATED FILTER LOGIC ---
    val filteredPlaces = remember(places, categoryFilter, radiusKm, userLocation, searchQuery) {
        val searchKey = normalizeForSearch(searchQuery.trim())
        places.filter { place ->
            val matchesCategory = categoryFilter == null || place.category == categoryFilter
            val matchesDistance = if (userLocation != null) {
                // 50 km means "todos os locais", so nothing is hidden at the top of the slider
                radiusKm >= 50f || calculateDistanceKm(userLocation!!, place.latLng) <= radiusKm
            } else {
                true // Show everything if we don't know where the user is
            }
            val matchesSearch = searchKey.isBlank() || normalizeForSearch(place.name).contains(searchKey)
            matchesCategory && matchesDistance && matchesSearch
        }.let { list ->
            // "Locais perto de ti": nearest first when the location is known
            val here = userLocation
            if (here == null) list else list.sortedBy { calculateDistanceKm(here, it.latLng) }
        }
    }

    val newPlaceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val name = data.getStringExtra("name") ?: ""
                val description = data.getStringExtra("description") ?: ""
                val lat = data.getDoubleExtra("lat", 0.0)
                val lng = data.getDoubleExtra("lng", 0.0)
                val category = data.getStringExtra("category") ?: ""
                val photoUris = data.getParcelableArrayListExtra<Uri>("photoUris")
                val manualAddress = data.getStringExtra("address") ?: ""

                if (name.isNotBlank() && lat != 0.0 && lng != 0.0) {
                    val newLatLng = LatLng(lat, lng)
                    val tempId = "temp_${System.currentTimeMillis()}"
                    val authorEmail = SessionManager.getUserEmail(context)

                    // Pre-save local photos
                    photoUris?.forEach { uri ->
                        PhotoRepository.insert(context, PhotoItem(reviewId = "PLACE-$tempId", placeId = tempId, userId = authorEmail, url = uri.toString(), status = PhotoStatus.APPROVED))
                    }

                    scope.launch {
                        val finalAddress = if (manualAddress.isNotBlank()) manualAddress else getAddressFromLatLng(context, newLatLng)
                        val catId = when (category) {
                            context.getString(R.string.category_atividades) -> 1L
                            context.getString(R.string.category_lojas) -> 2L
                            context.getString(R.string.category_restauracao) -> 3L
                            context.getString(R.string.category_historicos) -> 4L
                            context.getString(R.string.category_paisagens) -> 5L
                            else -> 1L
                        }

                        // Optimistic Update (Show immediately)
                        val newPlace = Place(
                            id = tempId, name = name, description = description, latLng = newLatLng,
                            category = category, authorId = authorEmail, address = finalAddress,
                            rating = 0.0, photoUris = photoUris?.map { it.toString() } ?: emptyList()
                        )
                        places = places + newPlace

                        try {
                            val apiRequest = PlaceRequest(
                                name = name,
                                description = description,
                                lat = lat,
                                lng = lng,
                                addressFull = finalAddress,
                                categoryId = catId,
                                authorId = authorEmail
                            )

                            val api = ApiClient.instance.create(PlaceApiService::class.java)
                            val savedPlace = api.createPlace(apiRequest)

                            // Important: Refresh to get real ID and correct state
                            refreshPlaces()

                            // Upload Photos
                            photoUris?.forEach { uri ->
                                scope.launch(Dispatchers.IO) {
                                    val response = PhotoUploadClient.uploadPhoto(context, uri, authorEmail, savedPlace.placeId.toString(), "PLACE-INITIAL")
                                    if (response?.url != null) {
                                        withContext(Dispatchers.Main) {
                                            // Save the official URL
                                            PhotoRepository.insert(context, PhotoItem(
                                                reviewId = "PLACE-${savedPlace.placeId}",
                                                placeId = savedPlace.placeId.toString(),
                                                userId = authorEmail,
                                                url = response.url,
                                                status = PhotoStatus.APPROVED
                                            ))
                                            // Refresh UI again to swap local photo for server photo if ready
                                            refreshPlaces()
                                        }
                                    }
                                }
                            }
                            // A place created by a USER waits for moderation.
                            val savedMessage = if (savedPlace.status == "PENDING") {
                                context.getString(R.string.place_pending_toast)
                            } else {
                                "Local guardado!"
                            }
                            Toast.makeText(context, savedMessage, Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Revert optimistic update on failure
                            places = places.filter { it.id != tempId }
                            withContext(Dispatchers.Main) { Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show() }
                        }
                    }
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions -> hasLocationPermission = permissions.values.any { it } }
    )

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        val anyGranted = permissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (!anyGranted) {
            locationPermissionLauncher.launch(permissions)
        }
    }

    // Move to the user's location on the first fix. The map already sits on the
    // default centre, so a fix or the timeout below only refines it.
    fun onLocationFix(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)
        userLocation = userLatLng
        if (!centeredOnUser) {
            centeredOnUser = true
            scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)) }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let { onLocationFix(it) }
                }
                // Ask for a fresh fix too; lastLocation is often null on a cold start.
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location -> location?.let { onLocationFix(it) } }
            } catch (e: SecurityException) { e.printStackTrace() }
        }
    }

    // Keep the position current while the map is open, so distances and the
    // "Estive aqui" check use where the user is now, not the first fix.
    DisposableEffect(hasLocationPermission) {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onLocationFix(it) }
            }
        }
        if (hasLocationPermission) {
            try {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()
                fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            } catch (e: SecurityException) { e.printStackTrace() }
        }
        onDispose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    // No permission or no fix within a few seconds: keep the default centre.
    LaunchedEffect(Unit) {
        delay(5000)
        if (!centeredOnUser) {
            centeredOnUser = true
            scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(defaultPosition, 14f)) }
        }
    }

    // Category colours stay the same in both modes; only the chip text colour changes.
    val categories = listOf(
        CategoryButtonInfo(stringResource(R.string.category_atividades), Icons.Outlined.Hiking, stringResource(R.string.category_atividades), CategoryBlue, Color.Black),
        CategoryButtonInfo(stringResource(R.string.category_lojas), Icons.Default.ShoppingCart, stringResource(R.string.category_lojas), CategoryYellow, Color.Black),
        CategoryButtonInfo(stringResource(R.string.category_restauracao), Icons.Default.Restaurant, stringResource(R.string.category_restauracao), CategoryOrange, Color.White),
        CategoryButtonInfo(stringResource(R.string.category_historicos), Icons.Outlined.AccountBalance, stringResource(R.string.category_historicos), CategoryMagenta, Color.White),
        CategoryButtonInfo(stringResource(R.string.category_paisagens), Icons.Outlined.Landscape, stringResource(R.string.category_paisagens), CategoryGreen, Color.White)
    )

    // --- FILTER DIALOG ---
    if (showFilterDialog) {
        Dialog(onDismissRequest = { showFilterDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Raio de Distância", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (radiusKm >= 50f) "Todos os locais (>50km)" else "${radiusKm.roundToInt()} km",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = radiusKm,
                        onValueChange = { radiusKm = it },
                        valueRange = 1f..50f,
                        steps = 48,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showFilterDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Aplicar")
                    }
                }
            }
        }
    }

    // System Back closes the FAB menu, then the place detail, then clears the
    // search focus; only when none applies does it exit the app.
    val focusManager = LocalFocusManager.current
    BackHandler(enabled = isActionMenuExpanded || selectedPlace != null || isSearchFocused) {
        when {
            isActionMenuExpanded -> isActionMenuExpanded = false
            selectedPlace != null -> selectedPlace = null
            else -> focusManager.clearFocus()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (selectedPlace != null) 300.dp else 80.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            if (selectedPlace == null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.bottomsheet_nearby_places), fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_info), tint = MaterialTheme.colorScheme.onSurface)
                    }
                    LazyColumn {
                        items(filteredPlaces) { place ->
                            // Own place, already confirmed by the server
                            val canDelete = place.id.toLongOrNull() != null &&
                                place.authorId.isNotBlank() &&
                                SessionManager.getUserEmail(context).equals(place.authorId, ignoreCase = true)
                            PlaceCard(
                                place = place,
                                canDelete = canDelete,
                                onClick = {
                                    selectedPlace = place
                                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(place.latLng, 16f)) }
                                },
                                onDelete = { deletePlace(place) },
                                onImageClick = { uri -> fullScreenImageUri = uri }
                            )
                        }
                    }
                }
            } else {
                PlaceDetailView(
                    place = selectedPlace!!,
                    userLocation = userLocation,
                    hasLocationPermission = hasLocationPermission,
                    onRequestLocationPermission = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onClose = { selectedPlace = null },
                    onImageClick = { uri -> fullScreenImageUri = uri },
                    // PASS CALLBACK TO REFRESH STARS ON REVIEW ADD
                    onReviewAdded = { refreshPlaces() }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = {
                    scope.launch {
                        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) scaffoldState.bottomSheetState.partialExpand()
                        else { selectedPlace = null; scaffoldState.bottomSheetState.partialExpand() }
                    }
                },
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, mapStyleResource()),
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, compassEnabled = false)
            ) {
                filteredPlaces.forEach { place ->
                    val color = when (place.category) {
                        stringResource(R.string.category_atividades) -> BitmapDescriptorFactory.HUE_AZURE
                        stringResource(R.string.category_lojas) -> BitmapDescriptorFactory.HUE_YELLOW
                        stringResource(R.string.category_restauracao) -> BitmapDescriptorFactory.HUE_ORANGE
                        stringResource(R.string.category_historicos) -> BitmapDescriptorFactory.HUE_MAGENTA
                        stringResource(R.string.category_paisagens), "Paisagens" -> BitmapDescriptorFactory.HUE_GREEN
                        else -> BitmapDescriptorFactory.HUE_RED
                    }
                    Marker(
                        state = MarkerState(position = place.latLng),
                        title = place.name,
                        snippet = place.description,
                        icon = BitmapDescriptorFactory.defaultMarker(color),
                        onClick = { selectedPlace = place; scope.launch { scaffoldState.bottomSheetState.expand() }; true }
                    )
                }
            }

            // --- SEARCH, CATEGORIES & FILTER BUTTON ---
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {
                // Search by name: filters both the nearby list and the markers.
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .onFocusChanged { isSearchFocused = it.isFocused },
                    placeholder = { Text(stringResource(R.string.search_places_hint), fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.cd_clear_search),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Filter Button
                    Button(
                        onClick = { showFilterDialog = true },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = stringResource(R.string.cd_filter), modifier = Modifier.size(20.dp))
                        if (radiusKm < 50f) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${radiusKm.toInt()}km", fontSize = 12.sp)
                        }
                    }

                    // 2. Category Buttons
                    categories.forEach { category ->
                        val isSelected = categoryFilter == category.text
                        Button(
                            onClick = { categoryFilter = if (isSelected) null else category.text },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = category.color, contentColor = category.contentColor),
                            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)) else null
                        ) {
                            Icon(imageVector = category.icon, contentDescription = category.contentDescription, tint = category.contentColor)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = category.text, color = category.contentColor, fontSize = 16.sp)
                        }
                    }
                }
            }

            // FABs (Right Side)
            Column(
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = 16.dp, top = 168.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = { isActionMenuExpanded = !isActionMenuExpanded },
                    containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = if (isActionMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = stringResource(
                            if (isActionMenuExpanded) R.string.cd_close_menu else R.string.cd_open_menu
                        )
                    )
                }
                if (isActionMenuExpanded) {
                    ExpandedActionButton(Icons.Default.Person, stringResource(R.string.fab_profile)) {
                        isActionMenuExpanded = false
                        val next = if (SessionManager.isLoggedIn(context)) ProfileActivity::class.java else LoginWelcomeBackActivity::class.java
                        context.startActivity(Intent(context, next))
                    }
                    ExpandedActionButton(Icons.Default.Camera, stringResource(R.string.fab_add_place)) {
                        isActionMenuExpanded = false
                        if (SessionManager.isLoggedIn(context)) newPlaceLauncher.launch(Intent(context, NewPlaceActivity::class.java))
                        else context.startActivity(Intent(context, LoginWelcomeBackActivity::class.java))
                    }
                    ExpandedActionButton(Icons.Default.LocalOffer, stringResource(R.string.fab_coupons)) {
                        isActionMenuExpanded = false
                        context.startActivity(Intent(context, CouponActivity::class.java))
                    }
                    ExpandedActionButton(Icons.Default.Directions, stringResource(R.string.fab_pedipaper)) {
                        isActionMenuExpanded = false
                        val next = if (SessionManager.isLoggedIn(context)) PedipaperActivity::class.java else LoginWelcomeBackActivity::class.java
                        context.startActivity(Intent(context, next))
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val ll = LatLng(it.latitude, it.longitude)
                                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(ll, 15f)) }
                                }
                            }
                        } catch (e: SecurityException) { e.printStackTrace() }
                    } else {
                        // Permission was denied before: ask for it again instead of doing nothing.
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 112.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.MyLocation, stringResource(R.string.cd_recenter_map))
            }

            if (fullScreenImageUri != null) {
                Dialog(onDismissRequest = { fullScreenImageUri = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable { fullScreenImageUri = null }) {
                        AsyncImage(model = fullScreenImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        IconButton(onClick = { fullScreenImageUri = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                            Icon(Icons.Default.Close, stringResource(R.string.cd_close), tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceCard(
    place: Place,
    canDelete: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: (Uri) -> Unit
) {
    val ctx = LocalContext.current

    val coverUrl = remember(place.id, place.photoUris) {
        place.photoUris.firstOrNull()
            ?: PhotoRepository.getByPlaceId(ctx, place.id).firstOrNull()?.url
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp)) {
                if (coverUrl != null) {
                    val coverUri = Uri.parse(coverUrl)

                    // --- UPDATED IMAGE LOADER FOR HTTP STRINGS ---
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(coverUrl) // Handle string directly
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick(coverUri) },
                        contentScale = ContentScale.Crop,
                        // Show red warning if loading fails (debugging aid)
                        error = rememberVectorPainter(Icons.Default.Warning),
                        onError = { state ->
                            Log.e("ImageLoad", "Failed to load: $coverUrl", state.result.throwable)
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
                }
                if (canDelete) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
                    ) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_delete_place),
                                tint = Color.White
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(place.category, color = Color.White, fontSize = 12.sp)
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(place.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.cd_location),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(place.address, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))

                // --- VISUAL STAR RATING (UPDATED) ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val ratingInt = place.rating.roundToInt() // Round to nearest integer for stars
                    repeat(5) { index ->
                        val isActive = index < ratingInt
                        Icon(
                            imageVector = if (isActive) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = if (isActive) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(String.format("%.1f", place.rating), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
        }
    }
}


@Composable
fun PlaceDetailView(
    place: Place,
    userLocation: LatLng?,
    hasLocationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onClose: () -> Unit,
    onImageClick: (Uri) -> Unit,
    onReviewAdded: () -> Unit // CALLBACK
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_about),
        stringResource(R.string.tab_reviews),
        stringResource(R.string.tab_photos)
    )

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var allPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var isFavorite by remember(place.id) { mutableStateOf(false) }
    var hasCheckedIn by remember(place.id) { mutableStateOf(false) }
    var isCheckingIn by remember { mutableStateOf(false) }

    // Helper to refresh data
    val refreshData = {
        scope.launch {
            try {
                val numericId = place.id.toLongOrNull()

                if (numericId != null) {
                    // It's a real ID, fetch from server
                    val reviewApi = ApiClient.instance.create(ReviewApiService::class.java)
                    val email = SessionManager.getUserEmail(context)
                    reviews = reviewApi.getReviewsByPlace(numericId, if(email.isBlank()) null else email)

                    val photoApi = ApiClient.instance.create(PhotoApiService::class.java)
                    val photoList = photoApi.getPhotosByPlace(numericId)
                    allPhotos = photoList.mapNotNull { it.url }
                } else {
                    // It's a temp ID, just show local photos
                    allPhotos = place.photoUris
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Send a reaction without crashing the screen on a network error
    fun sendReaction(reviewId: Long, type: String) {
        val email = SessionManager.getUserEmail(context)
        if (email.isBlank()) return
        scope.launch {
            try {
                ApiClient.instance.create(ReviewApiService::class.java)
                    .toggleReaction(ReactionRequest(email, reviewId, type))
                refreshData()
            } catch (e: IOException) {
                Toast.makeText(context, "Sem ligação. Tenta novamente.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Não foi possível registar a reação.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val writeReviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshData() // Refresh details view (new review)
            onReviewAdded() // Refresh MAIN list (stars average)
        }
    }

    // Initial Load
    LaunchedEffect(place.id) {
        refreshData()
    }

    val coverUrl = remember(allPhotos) { allPhotos.firstOrNull() }
    // A place only has a reviewable id once the server confirmed it
    val numericPlaceId = remember(place.id) { place.id.toLongOrNull() }

    // --- FAVOURITES ---
    LaunchedEffect(place.id, numericPlaceId) {
        val email = SessionManager.getUserEmail(context)
        if (email.isNotBlank() && numericPlaceId != null) {
            try {
                val favorites = ApiClient.instance.create(FavoriteApiService::class.java).getFavorites(email)
                isFavorite = favorites.any { it.placeId == numericPlaceId }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFavorite() {
        val email = SessionManager.getUserEmail(context)
        if (email.isBlank()) {
            context.startActivity(Intent(context, LoginWelcomeBackActivity::class.java))
            return
        }
        val placeId = numericPlaceId ?: return
        scope.launch {
            try {
                val api = ApiClient.instance.create(FavoriteApiService::class.java)
                if (isFavorite) {
                    val response = api.removeFavorite(email, placeId)
                    if (response.isSuccessful) isFavorite = false
                    else Toast.makeText(context, context.getString(R.string.favorite_remove_error), Toast.LENGTH_SHORT).show()
                } else {
                    val response = api.addFavorite(FavoriteRequest(email, placeId))
                    if (response.isSuccessful) isFavorite = true
                    else Toast.makeText(context, context.getString(R.string.favorite_add_error), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, context.getString(R.string.visit_error_network), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- DIRECTIONS AND SHARING ---
    fun openDirections() {
        val coords = "${place.latLng.latitude},${place.latLng.longitude}"
        val navigation = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$coords"))
            .setPackage("com.google.android.apps.maps")
        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$coords"))
        try {
            context.startActivity(navigation)
        } catch (e: ActivityNotFoundException) {
            runCatching { context.startActivity(fallback) }
        }
    }

    fun sharePlace() {
        val coords = "${place.latLng.latitude},${place.latLng.longitude}"
        val mapsLink = "https://www.google.com/maps/search/?api=1&query=$coords"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, place.name)
            putExtra(Intent.EXTRA_TEXT, "${place.name}\n${place.address}\n$mapsLink")
        }
        context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_place_title)))
    }

    // --- CHECK-IN ("Estive aqui") ---
    val distanceToPlace = userLocation?.let { calculateDistanceKm(it, place.latLng) * 1000f }
    val canCheckIn = numericPlaceId != null && hasLocationPermission &&
        distanceToPlace != null && distanceToPlace <= CHECKIN_RADIUS_METERS

    fun checkIn() {
        val email = SessionManager.getUserEmail(context)
        if (email.isBlank()) {
            context.startActivity(Intent(context, LoginWelcomeBackActivity::class.java))
            return
        }
        val placeId = numericPlaceId ?: return
        val location = userLocation ?: return
        if (isCheckingIn) return
        isCheckingIn = true
        scope.launch {
            try {
                val response = ApiClient.instance.create(VisitApiService::class.java)
                    .checkin(VisitRequest(email, placeId, location.latitude, location.longitude))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    SessionManager.saveUserPoints(context, body.newBalance)
                    hasCheckedIn = true
                    val message = if (body.pointsAwarded > 0) {
                        context.getString(R.string.visit_points_toast, body.pointsAwarded)
                    } else {
                        context.getString(R.string.visit_already_toast)
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                } else {
                    val error = parseServerError(response.errorBody()?.string())
                    Toast.makeText(context, error ?: context.getString(R.string.visit_error), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, context.getString(R.string.visit_error_network), Toast.LENGTH_LONG).show()
            } finally {
                isCheckingIn = false
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // --- HEADER ---
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(place.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    if (place.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    } else if (place.status == "PENDING") {
                        Spacer(modifier = Modifier.width(6.dp))
                        PendingBadge()
                    }
                }
                Text(place.address, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)

                val displayRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else place.rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(String.format("%.1f (%d avaliações)", displayRating, reviews.size), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
            if (coverUrl != null) {
                AsyncImage(
                    model = Uri.parse(coverUrl),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).clickable { onImageClick(Uri.parse(coverUrl)) },
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- ACTION ROW: favourite, directions, share, check-in ---
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            PlaceActionButton(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                label = stringResource(if (isFavorite) R.string.action_unfavorite else R.string.action_favorite),
                tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurface,
                enabled = numericPlaceId != null,
                onClick = { toggleFavorite() }
            )
            PlaceActionButton(
                icon = Icons.Default.Directions,
                label = stringResource(R.string.action_directions),
                onClick = { openDirections() }
            )
            PlaceActionButton(
                icon = Icons.Default.Share,
                label = stringResource(R.string.action_share),
                onClick = { sharePlace() }
            )
            PlaceActionButton(
                icon = if (hasCheckedIn) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                label = stringResource(R.string.action_checkin),
                tint = if (hasCheckedIn) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface,
                enabled = canCheckIn && !isCheckingIn,
                onClick = { checkIn() }
            )
        }

        // Explanation when "Estive aqui" cannot run yet.
        if (numericPlaceId != null && !hasCheckedIn && !canCheckIn) {
            val hint = when {
                !hasLocationPermission -> stringResource(R.string.checkin_need_permission)
                distanceToPlace == null -> stringResource(R.string.checkin_waiting_location)
                else -> stringResource(R.string.checkin_too_far, distanceToPlace.toInt())
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(hint, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // --- TABS ---
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- CONTENT ---
        when (selectedTab) {
            0 -> Text(place.description) // About
            1 -> { // Reviews
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (SessionManager.isLoggedIn(context)) {
                                val placeId = numericPlaceId ?: return@Button
                                val intent = Intent(context, NewReviewActivity::class.java).apply {
                                    putExtra("placeId", placeId)
                                    putExtra("placeName", place.name)
                                }
                                writeReviewLauncher.launch(intent)
                            } else {
                                val intent = Intent(context, LoginWelcomeBackActivity::class.java)
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = if (numericPlaceId == null) 4.dp else 16.dp),
                        enabled = numericPlaceId != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.write_review_btn_label))
                    }
                    if (numericPlaceId == null) {
                        Text(
                            "Local ainda a sincronizar. Tenta novamente dentro de momentos.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (reviews.isEmpty()) {
                        Text(stringResource(R.string.no_reviews_yet), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            items(reviews) { review ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // User Info
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            InitialsAvatar(
                                                name = review.userName,
                                                photoUrl = review.userPhotoUrl,
                                                size = 40.dp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(text = review.userName ?: "Utilizador", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                                Text(text = review.createdAt?.take(10) ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Rating
                                        Row {
                                            repeat(5) { index ->
                                                val isActive = index < review.rating
                                                Icon(imageVector = if (isActive) Icons.Filled.Star else Icons.Outlined.Star, contentDescription = null, tint = if (isActive) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        if (!review.title.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(text = review.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                        }
                                        if (!review.comment.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = review.comment, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        // Photos in Review
                                        if (review.photoUrls.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(review.photoUrls) { url ->
                                                    AsyncImage(
                                                        model = Uri.parse(url),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).clickable { onImageClick(Uri.parse(url)) },
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Reactions
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { sendReaction(review.reviewId, "USEFUL") }) {
                                                Icon(if (review.currentUserReaction == "USEFUL") Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp, stringResource(R.string.cd_mark_useful), tint = if (review.currentUserReaction == "USEFUL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text("${review.likesCount}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            IconButton(onClick = { sendReaction(review.reviewId, "NOT_USEFUL") }) {
                                                Icon(if (review.currentUserReaction == "NOT_USEFUL") Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown, stringResource(R.string.cd_mark_not_useful), tint = if (review.currentUserReaction == "NOT_USEFUL") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text("${review.dislikesCount}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> { // Photos Tab
                if (allPhotos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("Ainda sem fotos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allPhotos) { url ->
                            AsyncImage(
                                model = url.toUri(),
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onImageClick(url.toUri()) },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}