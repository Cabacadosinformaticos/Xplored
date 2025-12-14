package pt.iade.ei.xplored.ui.screens.place

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.ui.theme.XploredTheme
import java.util.Locale

class NewPlaceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XploredTheme {
                NewPlaceScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPlaceScreen() {
    val context = LocalContext.current
    var showMapView by remember { mutableStateOf(false) }

    // Form State
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // NEW: Address Field State
    var address by remember { mutableStateOf("") }

    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf(
        stringResource(R.string.category_atividades),
        stringResource(R.string.category_lojas),
        stringResource(R.string.category_restauracao),
        stringResource(R.string.category_historicos),
        stringResource(R.string.category_paisagens)
    )
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // --- GEOCODER LOGIC START ---
    // Whenever selectedLatLng changes (user picks a point), try to fetch the address.
    LaunchedEffect(selectedLatLng) {
        selectedLatLng?.let { latLng ->
            if (Geocoder.isPresent()) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    // Run in background to avoid freezing UI
                    withContext(Dispatchers.IO) {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val foundAddress = addresses[0].getAddressLine(0)
                            // Update UI on Main Thread
                            withContext(Dispatchers.Main) {
                                // Only overwrite if address field is empty or contains a previous auto-generated one
                                // But here we just overwrite to be helpful
                                address = foundAddress ?: ""
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    // --- GEOCODER LOGIC END ---

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            selectedImageUris = uris
            val contentResolver = context.contentResolver
            uris.forEach { uri ->
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    )

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            userLocation = LatLng(it.latitude, it.longitude)
                        }
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showMapView) stringResource(R.string.map_choose_location) else stringResource(
                    R.string.new_place_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showMapView) {
                            showMapView = false
                        } else {
                            (context as? Activity)?.finish()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showMapView) {
            val cameraPositionState = rememberCameraPositionState {
                val initialPosition = userLocation ?: selectedLatLng ?: LatLng(38.7223, -9.1393)
                position = CameraPosition.fromLatLngZoom(initialPosition, 15f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLatLng = latLng
                    showMapView = false
                },
                properties = MapProperties(
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                )
            ) {
                selectedLatLng?.let {
                    Marker(state = MarkerState(position = it))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.new_place_field_title), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.new_place_label_title)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.new_place_field_description), fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.new_place_label_description)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.new_place_field_category), fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.new_place_select_category)) },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.new_place_field_location), fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .clickable { showMapView = true }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedLatLng?.let { "Lat: %.4f, Lng: %.4f".format(it.latitude, it.longitude) } ?: stringResource(
                            R.string.new_place_label_location),
                        color = if (selectedLatLng == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(Icons.Outlined.Map, contentDescription = "Open Map")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- NEW ADDRESS FIELD ---
                // This allows the user to manually fix "Moscavide" errors
                Text("Morada (Address)", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Morada") },
                    placeholder = { Text("Ex: Rua de Moscavide, 10") },
                    modifier = Modifier.fillMaxWidth()
                )
                // -------------------------

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.new_place_field_photos), fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedImageUris) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = { multiplePhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.size(100.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_photo))
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        // Include 'address' in validity check
                        if (title.isNotBlank() && description.isNotBlank() && selectedLatLng != null) {
                            val resultIntent = Intent().apply {
                                putExtra("name", title)
                                putExtra("description", description)
                                putExtra("lat", selectedLatLng!!.latitude)
                                putExtra("lng", selectedLatLng!!.longitude)
                                putExtra("category", selectedCategory)
                                putExtra("address", address.ifBlank { "Morada desconhecida" })
                                putParcelableArrayListExtra("photoUris", ArrayList(selectedImageUris))
                            }
                            (context as? Activity)?.setResult(Activity.RESULT_OK, resultIntent)
                            (context as? Activity)?.finish()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = title.isNotBlank() && description.isNotBlank() && selectedLatLng != null
                ) {
                    Text(stringResource(R.string.new_place_create))
                }
            }
        }
    }
}