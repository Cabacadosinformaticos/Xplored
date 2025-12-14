package pt.iade.ei.xplored.ui.screens.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.data.models.photos.PhotoItem
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.PhotoStatus
import pt.iade.ei.xplored.data.models.places.Place
import pt.iade.ei.xplored.data.models.reviews.Review
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.PhotoApiService
import pt.iade.ei.xplored.network.PhotoUploadClient
import pt.iade.ei.xplored.network.PlaceApiService
import pt.iade.ei.xplored.network.PlaceRequest
import pt.iade.ei.xplored.network.ReactionRequest
import pt.iade.ei.xplored.network.ReviewApiService
import pt.iade.ei.xplored.repositories.PhotoRepository
import pt.iade.ei.xplored.repositories.PlaceRepository
import pt.iade.ei.xplored.ui.screens.auth.LoginWelcomeBackActivity
import pt.iade.ei.xplored.ui.screens.coupon.CouponActivity
import pt.iade.ei.xplored.ui.screens.pedipaper.PedipaperActivity
import pt.iade.ei.xplored.ui.screens.place.NewPlaceActivity
import pt.iade.ei.xplored.ui.screens.profile.ProfileActivity
import pt.iade.ei.xplored.ui.screens.reviews.NewReviewActivity
import pt.iade.ei.xplored.ui.theme.XploredTheme
import java.io.IOException
import java.util.Locale
import kotlin.math.roundToInt

data class CategoryButtonInfo(
    val text: String,
    val icon: ImageVector,
    val contentDescription: String,
    val color: Color
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
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E2835),
            modifier = Modifier.padding(end = 8.dp)
        )
        FloatingActionButton(
            onClick = onClick,
            containerColor = Color.White,
            contentColor = Color.Black,
            shape = RoundedCornerShape(50)
        ) {
            icon()
        }
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

    val lisbon = LatLng(38.7223, -9.1393)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(lisbon, 10f)
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    var hasLocationPermission by remember { mutableStateOf(true) }
    var isActionMenuExpanded by remember { mutableStateOf(false) }

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

    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var fullScreenImageUri by remember { mutableStateOf<Uri?>(null) }

    // --- FILTER STATE ---
    var radiusKm by remember { mutableFloatStateOf(50f) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // --- UPDATED FILTER LOGIC ---
    val filteredPlaces = remember(places, categoryFilter, radiusKm, userLocation) {
        places.filter { place ->
            val matchesCategory = categoryFilter == null || place.category == categoryFilter
            val matchesDistance = if (userLocation != null) {
                calculateDistanceKm(userLocation!!, place.latLng) <= radiusKm
            } else {
                true // Show everything if we don't know where the user is
            }
            matchesCategory && matchesDistance
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
                            Toast.makeText(context, "Local salvo!", Toast.LENGTH_SHORT).show()
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
        onResult = { permissions -> hasLocationPermission = permissions.values.all { it } }
    )

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            hasLocationPermission = true
        } else {
            locationPermissionLauncher.launch(permissions)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        userLocation = userLatLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
                    }
                }
            } catch (e: SecurityException) { e.printStackTrace() }
        }
    }

    val categories = listOf(
        CategoryButtonInfo(stringResource(R.string.category_atividades), Icons.Outlined.Hiking, stringResource(R.string.category_atividades), Color(0xFF4192FF)),
        CategoryButtonInfo(stringResource(R.string.category_lojas), Icons.Default.ShoppingCart, stringResource(R.string.category_lojas), Color(0xFFFFEB3B)),
        CategoryButtonInfo(stringResource(R.string.category_restauracao), Icons.Default.Restaurant, stringResource(R.string.category_restauracao), Color(0xFFE65100)),
        CategoryButtonInfo(stringResource(R.string.category_historicos), Icons.Outlined.AccountBalance, stringResource(R.string.category_historicos), Color(0xFF880E4F)),
        CategoryButtonInfo(stringResource(R.string.category_paisagens), Icons.Outlined.Landscape, stringResource(R.string.category_paisagens), Color(0xFF3D6E44))
    )

    // --- FILTER DIALOG ---
    if (showFilterDialog) {
        Dialog(onDismissRequest = { showFilterDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
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

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (selectedPlace != null) 300.dp else 80.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContainerColor = Color.White,
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
                        Text(stringResource(R.string.bottomsheet_nearby_places), fontSize = 18.sp, color = Color.Black)
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.cd_info), tint = Color.Black)
                    }
                    LazyColumn {
                        items(filteredPlaces) { place ->
                            PlaceCard(
                                place = place,
                                onClick = {
                                    selectedPlace = place
                                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(place.latLng, 16f)) }
                                },
                                onDelete = {
                                    PlaceRepository.removePlace(context, place.id)
                                    places = PlaceRepository.getPlaces().toList()
                                },
                                onImageClick = { uri -> fullScreenImageUri = uri }
                            )
                        }
                    }
                }
            } else {
                PlaceDetailView(
                    place = selectedPlace!!,
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
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style),
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, compassEnabled = false)
            ) {
                filteredPlaces.forEach { place ->
                    val color = when (place.category) {
                        stringResource(R.string.category_atividades) -> BitmapDescriptorFactory.HUE_AZURE
                        stringResource(R.string.category_lojas) -> BitmapDescriptorFactory.HUE_YELLOW
                        stringResource(R.string.category_restauracao) -> BitmapDescriptorFactory.HUE_ORANGE
                        stringResource(R.string.category_historicos) -> BitmapDescriptorFactory.HUE_MAGENTA
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

            // --- CATEGORIES & FILTER BUTTON ---
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Filter Button
                Button(
                    onClick = { showFilterDialog = true },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Filter", modifier = Modifier.size(20.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = category.color),
                        border = if (isSelected) BorderStroke(2.dp, Color.Black.copy(alpha = 0.8f)) else null
                    ) {
                        Icon(imageVector = category.icon, contentDescription = category.contentDescription, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = category.text, color = Color.Black, fontSize = 16.sp)
                    }
                }
            }

            // FABs (Right Side)
            Column(
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = 16.dp, top = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = { isActionMenuExpanded = !isActionMenuExpanded },
                    containerColor = Color.White, contentColor = Color(0xFF1E2835), shape = RoundedCornerShape(50)
                ) {
                    Icon(imageVector = if (isActionMenuExpanded) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
                }
                if (isActionMenuExpanded) {
                    ExpandedActionButton({ Icon(Icons.Default.Person, null) }, stringResource(R.string.fab_profile)) {
                        val next = if (SessionManager.isLoggedIn(context)) ProfileActivity::class.java else LoginWelcomeBackActivity::class.java
                        context.startActivity(Intent(context, next))
                    }
                    ExpandedActionButton({ Icon(Icons.Default.Camera, null) }, stringResource(R.string.fab_add_place)) {
                        if (SessionManager.isLoggedIn(context)) newPlaceLauncher.launch(Intent(context, NewPlaceActivity::class.java))
                        else context.startActivity(Intent(context, LoginWelcomeBackActivity::class.java))
                    }
                    ExpandedActionButton({ Icon(Icons.Default.LocalOffer, null) }, stringResource(R.string.fab_coupons)) {
                        context.startActivity(Intent(context, CouponActivity::class.java))
                    }
                    ExpandedActionButton({ Icon(Icons.Default.Directions, null) }, stringResource(R.string.fab_pedipaper)) {
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
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 112.dp, end = 16.dp),
                containerColor = Color.White, contentColor = Color(0xFF1E2835), shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.MyLocation, stringResource(R.string.cd_recenter_map))
            }

            if (fullScreenImageUri != null) {
                Dialog(onDismissRequest = { fullScreenImageUri = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable { fullScreenImageUri = null }) {
                        AsyncImage(model = fullScreenImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        IconButton(onClick = { fullScreenImageUri = null }, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.White)
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete_place),
                            tint = Color.White
                        )
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
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(place.address, color = Color.Gray, fontSize = 14.sp)
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
                            tint = if (isActive) Color(0xFFFFC107) else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(String.format("%.1f", place.rating), color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}


@Composable
fun PlaceDetailView(
    place: Place,
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

    Column(modifier = Modifier.padding(16.dp)) {
        // --- HEADER ---
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(place.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    if (place.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Text(place.address, color = Color.Gray, fontSize = 14.sp)

                val displayRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else place.rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(String.format("%.1f (%d reviews)", displayRating, reviews.size), color = Color.Gray, fontSize = 14.sp)
                }
            }
            if (coverUrl != null) {
                AsyncImage(
                    model = Uri.parse(coverUrl),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).background(Color.Gray, RoundedCornerShape(8.dp)).clickable { onImageClick(Uri.parse(coverUrl)) },
                    contentScale = ContentScale.Crop
                )
            }
        }

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
                                val intent = Intent(context, NewReviewActivity::class.java).apply {
                                    val numericId = place.id.toLongOrNull() ?: 0L
                                    putExtra("placeId", numericId)
                                    putExtra("placeName", place.name)
                                }
                                writeReviewLauncher.launch(intent)
                            } else {
                                val intent = Intent(context, LoginWelcomeBackActivity::class.java)
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2835))
                    ) {
                        Text(stringResource(R.string.write_review_btn_label))
                    }

                    if (reviews.isEmpty()) {
                        Text(stringResource(R.string.no_reviews_yet), color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            items(reviews) { review ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // User Info
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!review.userPhotoUrl.isNullOrBlank()) {
                                                AsyncImage(model = review.userPhotoUrl, contentDescription = "Avatar", modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape), contentScale = ContentScale.Crop)
                                            } else {
                                                Icon(Icons.Default.AccountCircle, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(text = review.userName ?: "Utilizador", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E2835))
                                                Text(text = review.createdAt?.take(10) ?: "", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Rating
                                        Row {
                                            repeat(5) { index ->
                                                val isActive = index < review.rating
                                                Icon(imageVector = if (isActive) Icons.Filled.Star else Icons.Outlined.Star, contentDescription = null, tint = if (isActive) Color(0xFFFFC107) else Color.LightGray, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        if (!review.title.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(text = review.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                        }
                                        if (!review.comment.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = review.comment, fontSize = 14.sp, color = Color(0xFF4A4A4A))
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
                                            IconButton(onClick = {
                                                val email = SessionManager.getUserEmail(context)
                                                if (email.isNotBlank()) {
                                                    scope.launch {
                                                        ApiClient.instance.create(ReviewApiService::class.java).toggleReaction(ReactionRequest(email, review.reviewId, "USEFUL"))
                                                        refreshData()
                                                    }
                                                }
                                            }) {
                                                Icon(if (review.currentUserReaction == "USEFUL") Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp, null, tint = if (review.currentUserReaction == "USEFUL") MaterialTheme.colorScheme.primary else Color.Gray)
                                            }
                                            Text("${review.likesCount}", fontSize = 14.sp, color = Color.Gray)
                                            Spacer(modifier = Modifier.width(16.dp))
                                            IconButton(onClick = {
                                                val email = SessionManager.getUserEmail(context)
                                                if (email.isNotBlank()) {
                                                    scope.launch {
                                                        ApiClient.instance.create(ReviewApiService::class.java).toggleReaction(ReactionRequest(email, review.reviewId, "NOT_USEFUL"))
                                                        refreshData()
                                                    }
                                                }
                                            }) {
                                                Icon(if (review.currentUserReaction == "NOT_USEFUL") Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown, null, tint = if (review.currentUserReaction == "NOT_USEFUL") Color.Red else Color.Gray)
                                            }
                                            Text("${review.dislikesCount}", fontSize = 14.sp, color = Color.Gray)
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
                        Text("Ainda sem fotos.", color = Color.Gray)
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