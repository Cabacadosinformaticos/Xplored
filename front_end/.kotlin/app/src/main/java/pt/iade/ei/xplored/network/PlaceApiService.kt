package pt.iade.ei.xplored.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import pt.iade.ei.xplored.data.models.places.Place

data class PlaceResponse(
    val placeId: Long,
    val name: String,
    val description: String?,
    val lat: Double,
    val lng: Double,
    val addressFull: String?,
    val categoryId: Long,
    val avgRating: Double?,
    val coverImageUrl: String?
)

interface PlaceApiService {
    // FIX: Changed from "place/save" to "places" to match PlaceController
    @POST("places")
    suspend fun createPlace(@Body request: PlaceRequest): PlaceResponse

    // FIX: Changed from "place/get-all" to "places" to match PlaceController
    @GET("places")
    suspend fun getAllPlaces(): List<PlaceResponse>
}