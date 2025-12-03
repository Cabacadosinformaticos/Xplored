package pt.iade.ei.xplored.repositories

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.iade.ei.xplored.R
import pt.iade.ei.xplored.data.models.photos.PhotoItem
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.PhotoKind
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.PhotoStatus
import pt.iade.ei.xplored.data.models.places.Place
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.PlaceApiService

object PlaceRepository {

    // Cached list for the UI to read instantly. Replaced, never mutated in place,
    // so Compose observes a new list value on every refresh.
    private var _places: List<Place> = emptyList()

    fun getPlaces(): List<Place> {
        return _places.toList()
    }

    /**
     * Downloads places from Spring Boot and updates the local list.
     * Includes a fallback check: if server has no image, check local DB.
     */
    suspend fun fetchPlacesFromBackend(context: Context): List<Place> {
        return withContext(Dispatchers.IO) {
            try {
                val api = ApiClient.instance.create(PlaceApiService::class.java)
                val networkPlaces = api.getAllPlaces()

                // LOGGING: Check what we received
                android.util.Log.d("PlaceRepo", "Received ${networkPlaces.size} places from backend.")

                val refreshed = mutableListOf<Place>()

                networkPlaces.forEach { np ->
                    // LOGGING: Print URL for each place
                    android.util.Log.d("PlaceRepo", "Place: ${np.name}, CoverURL: ${np.coverImageUrl}")

                    val catName = when (np.categoryId) {
                        1L -> context.getString(R.string.category_atividades)
                        2L -> context.getString(R.string.category_lojas)
                        3L -> context.getString(R.string.category_restauracao)
                        4L -> context.getString(R.string.category_historicos)
                        5L -> context.getString(R.string.category_paisagens)
                        else -> "Outro"
                    }

                    val placeId = np.placeId.toString()
                    val author = np.authorId ?: "Server"
                    val localPhotos = PhotoRepository.getByPlaceId(context, placeId)

                    // Store the server cover locally once, so repeated refreshes do not
                    // pile up duplicate rows for the same place.
                    val cover = np.coverImageUrl
                    if (!cover.isNullOrBlank() && localPhotos.none { it.url == cover }) {
                        withContext(Dispatchers.Main) {
                            PhotoRepository.insert(
                                context,
                                PhotoItem(
                                    reviewId = "PLACE-${np.placeId}",
                                    placeId = placeId,
                                    userId = author,
                                    url = cover,
                                    status = PhotoStatus.APPROVED,
                                    kind = PhotoKind.GALLERY
                                )
                            )
                        }
                    }

                    val displayPhotos = if (!cover.isNullOrBlank()) {
                        listOf(cover)
                    } else {
                        localPhotos.map { it.url }
                    }

                    val uiPlace = Place(
                        id = placeId,
                        name = np.name,
                        description = np.description ?: "",
                        latLng = LatLng(np.lat, np.lng),
                        address = np.addressFull ?: "",
                        category = catName,
                        rating = np.avgRating ?: 0.0,
                        authorId = author,
                        isVerified = true,
                        photoUris = displayPhotos
                    )
                    refreshed.add(uiPlace)
                }

                _places = refreshed
                return@withContext _places
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList()
            }
        }
    }
    fun addPlace(context: Context, place: Place) {
        _places = _places + place
    }

    fun removePlace(context: Context, id: String) {
        _places = _places.filterNot { it.id == id }
    }
}