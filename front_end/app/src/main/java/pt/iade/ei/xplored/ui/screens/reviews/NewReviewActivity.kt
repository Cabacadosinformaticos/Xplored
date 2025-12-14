package pt.iade.ei.xplored.ui.screens.reviews

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.SessionManager
import pt.iade.ei.xplored.data.models.photos.PhotoItem
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.PhotoStatus
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.PhotoUploadClient
import pt.iade.ei.xplored.network.ReviewApiService
import pt.iade.ei.xplored.network.ReviewRequest
import pt.iade.ei.xplored.repositories.PhotoRepository
import pt.iade.ei.xplored.ui.theme.XploredTheme

class NewReviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure ID is passed as a Long (or convert if it's a string)
        val placeId = intent.getLongExtra("placeId", 0L)
        val placeName = intent.getStringExtra("placeName") ?: "Local"

        setContent {
            XploredTheme {
                NewReviewScreen(placeId, placeName)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReviewScreen(placeId: Long, placeName: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form State
    var rating by remember { mutableIntStateOf(0) }
    var title by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Photo State
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        selectedImages = uris
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = stringResource(R.string.write_review_title), fontSize = 18.sp)
                        Text(text = placeName, fontSize = 14.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rating
            Text(stringResource(R.string.review_score_label), fontWeight = FontWeight.Bold)
            StarRatingInput(rating) { rating = it }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.review_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comment
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text(stringResource(R.string.review_comment_label)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- PHOTO UPLOAD SECTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Adicionar Fotos", fontWeight = FontWeight.Bold)
                IconButton(onClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Preview Photos
            if (selectedImages.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedImages) { uri ->
                        Box(modifier = Modifier.size(80.dp)) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.Red,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(Color.White, androidx.compose.foundation.shape.CircleShape)
                                    .clickable { selectedImages = selectedImages - uri }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = {
                    if (rating > 0 && title.isNotBlank()) {
                        isSubmitting = true
                        scope.launch {
                            try {
                                val userEmail = SessionManager.getUserEmail(context)
                                if (userEmail.isBlank()) {
                                    Toast.makeText(context, "Erro: Login necessário.", Toast.LENGTH_LONG).show()
                                    isSubmitting = false
                                    return@launch
                                }

                                // 1. Create Review Text
                                val request = ReviewRequest(
                                    userEmail = userEmail,
                                    placeId = placeId,
                                    rating = rating,
                                    title = title,
                                    comment = comment
                                )
                                val api = ApiClient.instance.create(ReviewApiService::class.java)
                                val createdReview = api.createReview(request)

                                // 2. Upload Photos linked to this review
                                selectedImages.forEach { uri ->
                                    val response = PhotoUploadClient.uploadPhoto(
                                        context = context,
                                        uri = uri,
                                        userId = userEmail,
                                        placeId = placeId.toString(),
                                        reviewId = createdReview.reviewId.toString()
                                    )

                                    // Save local copy for fast loading
                                    if(response != null) {
                                        withContext(Dispatchers.Main) {
                                            PhotoRepository.insert(context, PhotoItem(
                                                reviewId = createdReview.reviewId.toString(),
                                                placeId = placeId.toString(),
                                                userId = userEmail,
                                                url = response.url ?: uri.toString(),
                                                status = PhotoStatus.APPROVED
                                            ))
                                        }
                                    }
                                }

                                Toast.makeText(context, "Avaliação enviada!", Toast.LENGTH_SHORT).show()
                                (context as? Activity)?.setResult(Activity.RESULT_OK)
                                (context as? Activity)?.finish()

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    } else {
                        Toast.makeText(context, "Preencha a classificação e o título.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isSubmitting && rating > 0 && title.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(stringResource(R.string.review_submit_btn))
                }
            }
        }
    }
}

@Composable
fun StarRatingInput(currentRating: Int, onRatingChanged: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center) {
        for (i in 1..5) {
            val isSelected = i <= currentRating
            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "$i Stars",
                tint = if (isSelected) Color(0xFFFFC107) else Color.LightGray,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onRatingChanged(i) }
                    .padding(4.dp)
            )
        }
    }
}