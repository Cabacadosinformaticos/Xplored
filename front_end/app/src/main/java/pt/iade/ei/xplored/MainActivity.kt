package pt.iade.ei.xplored


import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.clip
import pt.iade.ei.xplored.models.PhotoRepository
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material3.*
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import java.io.IOException
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.iade.ei.xplored.models.PhotoItem
import pt.iade.ei.xplored.models.PhotoStatus
import pt.iade.ei.xplored.models.Place
import pt.iade.ei.xplored.models.PlaceApiRepository
import pt.iade.ei.xplored.models.PlaceRepository
import pt.iade.ei.xplored.ui.theme.XploredTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import pt.iade.ei.xplored.models.CategoryMapper


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
        return context.getString(R.string.geocoder_not_available) // from strings.xml
    }
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        }
        if (addresses != null && addresses.isNotEmpty()) {
            addresses[0].getAddressLine(0) ?: context.getString(R.string.address_not_found) // from strings.xml
        } else {
            context.getString(R.string.address_not_found) // from strings.xml
        }
    } catch (e: IOException) {
        e.printStackTrace()
        context.getString(R.string.address_could_not_retrieve) // from strings.xml
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
    var places by remember { mutableStateOf(PlaceRepository.getPlaces().toList()) }


    // Fetch places from backend on launch
    LaunchedEffect(Unit) {
        PlaceApiRepository.fetchAllPlaces { apiPlaces ->
            if (apiPlaces != null) {
                val convertedPlaces = apiPlaces.map { p ->
                    Place(
                        name = p.name,
                        description = p.description,
                        latLng = com.google.android.gms.maps.model.LatLng(p.lat, p.lng),
                        category = "Unknown", // until categoryId mapping is added
                        authorId = "1",
                        address = p.addressFull ?: "",
                        rating = p.categoryId?.toDouble() ?: 0.0
                    )
                }
                places = convertedPlaces
            } else {
                android.widget.Toast.makeText(
                    context,
                    "Failed to load places from database",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var categoryFilter by remember { mutableStateOf<String?>(null) }

    // Fullscreen image state
    var fullScreenImageUri by remember { mutableStateOf<Uri?>(null) }

    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Reload places when MainView becomes visible again
                PlaceApiRepository.fetchAllPlaces { apiPlaces ->
                    if (apiPlaces != null) {
                        val convertedPlaces = apiPlaces.map { p ->
                            Place(
                                name = p.name,
                                description = p.description,
                                latLng = com.google.android.gms.maps.model.LatLng(p.lat, p.lng),
                                category = CategoryMapper.getNameById(p.categoryId),
                                authorId = "1",
                                address = p.addressFull ?: "",
                                rating = p.categoryId?.toDouble() ?: 0.0
                            )
                        }
                        places = PlaceRepository.getPlaces().toList() + convertedPlaces

                    } else {
                        android.widget.Toast.makeText(
                            context,
                            "Failed to refresh places",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }


    val filteredPlaces = remember(places, categoryFilter) {
        places.filter { place ->
            categoryFilter == null || place.category == categoryFilter
        }
    }

    val newPlaceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                val name = data.getStringExtra("name")
                val description = data.getStringExtra("description")
                val lat = data.getDoubleExtra("lat", 0.0)
                val lng = data.getDoubleExtra("lng", 0.0)
                val category = data.getStringExtra("category")
                val photoUris = data.getParcelableArrayListExtra<Uri>("photoUris")

                if (name != null && description != null && lat != 0.0 && lng != 0.0 && category != null) {
                    val newLatLng = LatLng(lat, lng)
                    scope.launch {
                        val address = getAddressFromLatLng(context, newLatLng)
                        val newPlace = Place(
                            name = name,
                            description = description,
                            latLng = newLatLng,
                            category = category,
                            authorId = "1", // TODO replace with actual user ID
                            address = address,
                            rating = 4.8
                        )
                        PlaceRepository.addPlace(context, newPlace)

                        // Persist place-linked photos into PhotoRepository
                        photoUris?.forEach { uri ->
                            PhotoRepository.insert(
                                context,
                                PhotoItem(
                                    reviewId = "PLACE-${newPlace.id}",
                                    placeId = newPlace.id,
                                    userId = newPlace.authorId,
                                    url = uri.toString(),
                                    status = PhotoStatus.APPROVED
                                )
                            )
                        }

                        withContext(Dispatchers.Main) {
                            places = PlaceRepository.getPlaces().toList()
                        }
                    }
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions.values.all { it }
        }
    )

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val permissionsGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (permissionsGranted) {
            hasLocationPermission = true
        } else {
            locationPermissionLauncher.launch(permissions)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@LaunchedEffect
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
                }
            }
        }
    }

    val categories = listOf(
        CategoryButtonInfo(stringResource(R.string.category_atividades), Icons.Outlined.Hiking, stringResource(R.string.category_atividades), Color(0xFF4192FF)),
        CategoryButtonInfo(stringResource(R.string.category_lojas), Icons.Default.ShoppingCart, stringResource(R.string.category_lojas), Color(0xFFFFEB3B)),
        CategoryButtonInfo(stringResource(R.string.category_restauracao), Icons.Default.Restaurant, stringResource(R.string.category_restauracao), Color(0xFFE65100)),
        CategoryButtonInfo(stringResource(R.string.category_historicos), Icons.Outlined.AccountBalance, stringResource(R.string.category_historicos), Color(0xFF880E4F)),
        CategoryButtonInfo(stringResource(R.string.category_paisagens), Icons.Outlined.Landscape, stringResource(R.string.category_paisagens), Color(0xFF3D6E44))
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = if (selectedPlace != null) 300.dp else 80.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContainerColor = Color.White,
        sheetContent = {
            if (selectedPlace == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                onClick = { selectedPlace = place },
                                onDelete = {
                                    PlaceRepository.removePlace(context, place.id)
                                    places = PlaceRepository.getPlaces().toList()
                                },
                                // Open full-screen when cover is tapped
                                onImageClick = { uri -> fullScreenImageUri = uri }
                            )
                        }
                    }
                }
            } else {
                PlaceDetailView(
                    place = selectedPlace!!,
                    onClose = { selectedPlace = null },
                    // Open full-screen when any photo is tapped
                    onImageClick = { uri -> fullScreenImageUri = uri }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = {
                    val state = scaffoldState.bottomSheetState
                    scope.launch {
                        when (state.currentValue) {
                            androidx.compose.material3.SheetValue.Expanded -> {
                                // Expanded to partial
                                state.partialExpand()
                            }
                            androidx.compose.material3.SheetValue.PartiallyExpanded -> {
                                // Partial to peek; clear selection
                                selectedPlace = null
                                state.partialExpand()
                            }
                            else -> {
                                // Already at peek; no action
                            }
                        }
                    }
                },
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false
                )
            )
            {
                filteredPlaces.forEach { place ->
                    val color = when (place.category) {
                        stringResource(R.string.category_atividades) -> BitmapDescriptorFactory.HUE_AZURE
                        stringResource(R.string.category_lojas) -> BitmapDescriptorFactory.HUE_YELLOW
                        stringResource(R.string.category_restauracao) -> BitmapDescriptorFactory.HUE_ORANGE
                        stringResource(R.string.category_historicos) -> BitmapDescriptorFactory.HUE_MAGENTA
                        stringResource(R.string.category_paisagens) -> BitmapDescriptorFactory.HUE_GREEN
                        else -> BitmapDescriptorFactory.HUE_RED
                    }
                    Marker(
                        state = MarkerState(position = place.latLng),
                        title = place.name,
                        snippet = place.description,
                        icon = BitmapDescriptorFactory.defaultMarker(color),
                        onClick = {
                            selectedPlace = place
                            scope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                            true
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = categoryFilter == category.text
                    Button(
                        onClick = { categoryFilter = if (isSelected) null else category.text },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = category.color),
                        border = if (isSelected) BorderStroke(2.dp, Color.Black.copy(alpha = 0.8f)) else null
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.contentDescription,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = category.text,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 16.dp, top = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                val anchorColor = Color.White
                val contentColor = Color(0xFF1E2835)

                FloatingActionButton(
                    onClick = { isActionMenuExpanded = !isActionMenuExpanded },
                    containerColor = anchorColor,
                    contentColor = contentColor,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = if (isActionMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isActionMenuExpanded) stringResource(R.string.cd_close_menu) else stringResource(R.string.cd_open_menu),
                    )
                }

                if (isActionMenuExpanded) {
                    ExpandedActionButton(
                        icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.fab_profile)) },
                        label = stringResource(R.string.fab_profile),
                        onClick = {
                            val nextActivity = if (SessionManager.isLoggedIn(context)) {
                                ProfileActivity::class.java
                            } else {
                                LoginWelcomeBackActivity::class.java
                            }
                            context.startActivity(Intent(context, nextActivity))
                        }
                    )

                    ExpandedActionButton(
                        icon = { Icon(Icons.Default.Camera, contentDescription = stringResource(R.string.fab_add_place)) },
                        label = stringResource(R.string.fab_add_place),
                        onClick = {
                            if (SessionManager.isLoggedIn(context)) {
                                val intent = Intent(context, NewPlaceActivity::class.java)
                                newPlaceLauncher.launch(intent)
                            } else {
                                val intent = Intent(context, LoginWelcomeBackActivity::class.java)
                                context.startActivity(intent)
                            }
                        }
                    )

                    ExpandedActionButton(
                        icon = { Icon(Icons.Default.LocalOffer, contentDescription = stringResource(R.string.fab_coupons)) },
                        label = stringResource(R.string.fab_coupons),
                        onClick = {
                            val intent = Intent(context, CouponActivity::class.java)
                            context.startActivity(intent)
                        }
                    )

                    ExpandedActionButton(
                        icon = { Icon(Icons.Default.Directions, contentDescription = stringResource(R.string.fab_pedipaper)) },
                        label = stringResource(R.string.fab_pedipaper),
                        onClick = {
                            val nextActivity = if (SessionManager.isLoggedIn(context)) {
                                PedipaperActivity::class.java
                            } else {
                                LoginWelcomeBackActivity::class.java
                            }
                            context.startActivity(Intent(context, nextActivity))
                        }
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return@FloatingActionButton
                        }
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            location?.let {
                                val userLatLng = LatLng(it.latitude, it.longitude)
                                scope.launch {
                                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 112.dp, end = 16.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF1E2835),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.MyLocation, stringResource(R.string.cd_recenter_map)) // from strings.xml
            }

            // Fullscreen image viewer
            if (fullScreenImageUri != null) {
                Dialog(
                    onDismissRequest = { fullScreenImageUri = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .clickable { fullScreenImageUri = null }
                    ) {
                        AsyncImage(
                            model = fullScreenImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { fullScreenImageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_close_menu), // reuse existing string
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Supports onImageClick
@Composable
fun PlaceCard(
    place: Place,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: (Uri) -> Unit
) {
    val ctx = LocalContext.current
    val coverUrl = remember(place.id) {
        PhotoRepository.getByPlaceId(ctx, place.id).firstOrNull()?.url
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp)) {
                if (coverUrl != null) {
                    val coverUri = Uri.parse(coverUrl)
                    AsyncImage(
                        model = coverUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick(coverUri) }, // Open full-screen on tap
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.cd_delete_place), // from strings.xml
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
                        contentDescription = stringResource(R.string.cd_location), // from strings.xml
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(place.address, color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = stringResource(R.string.cd_rating), // from strings.xml
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(place.rating.toString(), color = Color.Gray, fontSize = 14.sp)
                }
            }
        }
    }
}

// Supports onImageClick and repository-backed photos
@Composable
fun PlaceDetailView(place: Place, onClose: () -> Unit, onImageClick: (Uri) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(stringResource(R.string.tab_about), stringResource(R.string.tab_reviews), stringResource(R.string.tab_photos))

    val ctx = LocalContext.current
    var placePhotos by remember(place.id) { mutableStateOf<List<Uri>>(emptyList()) }
    LaunchedEffect(place.id) {
        placePhotos = PhotoRepository.getByPlaceId(ctx, place.id).map { Uri.parse(it.url) }
    }
    val coverUrl = remember(place.id, placePhotos) { placePhotos.firstOrNull()?.toString() }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(place.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    if (place.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.cd_verified), // from strings.xml
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.cd_location), // from strings.xml
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(place.address, color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = stringResource(R.string.cd_rating), // from strings.xml
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(place.rating.toString(), color = Color.Gray, fontSize = 14.sp)
                }
            }
            if (coverUrl != null) {
                val coverUri = Uri.parse(coverUrl)
                AsyncImage(
                    model = coverUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Gray, RoundedCornerShape(8.dp))
                        .clickable { onImageClick(coverUri) }, // Header tap opens full-screen
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (selectedTab) {
            0 -> {
                Text(place.description)
            }
            1 -> {
                Text(stringResource(R.string.placeholder_reviews_here)) // from strings.xml
            }
            2 -> {
                // 3x3 grid of photos; scrolls inside the bottom sheet
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    content = {
                        items(placePhotos) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)                           // square cells
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onImageClick(uri) },          // retain full-screen tap
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    XploredTheme {
        MainView()
    }
}
