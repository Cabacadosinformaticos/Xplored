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

    // Keep your local list for the UI to read instantly
    private val _places = mutableListOf<Place>()

    fun getPlaces(): List<Place> {
        return _places
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

                _places.clear()

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

                    if (!np.coverImageUrl.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            PhotoRepository.insert(
                                context,
                                PhotoItem(
                                    reviewId = "PLACE-${np.placeId}",
                                    placeId = np.placeId.toString(),
                                    userId = "Server",
                                    url = np.coverImageUrl,
                                    status = PhotoStatus.APPROVED,
                                    kind = PhotoKind.GALLERY
                                )
                            )
                        }
                    }

                    val localPhotos = PhotoRepository.getByPlaceId(context, np.placeId.toString())

                    val displayPhotos = if (!np.coverImageUrl.isNullOrBlank()) {
                        listOf(np.coverImageUrl)
                    } else if (localPhotos.isNotEmpty()) {
                        localPhotos.map { it.url }
                    } else {
                        emptyList()
                    }

                    val uiPlace = Place(
                        id = np.placeId.toString(),
                        name = np.name,
                        description = np.description ?: "",
                        latLng = LatLng(np.lat, np.lng),
                        address = np.addressFull ?: "",
                        category = catName,
                        rating = np.avgRating ?: 0.0,
                        authorId = "Server",
                        isVerified = true,
                        photoUris = displayPhotos
                    )
                    _places.add(uiPlace)
                }

                return@withContext _places
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList()
            }
        }
    }
    fun addPlace(context: Context, place: Place) {
        _places.add(place)
    }

    fun removePlace(context: Context, id: String) {
        _places.removeAll { it.id == id }
    }
}