package pt.iade.ei.xplored.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class PlaceResponse(
    val placeId: Long,
    val name: String,
    val description: String?,
    val lat: Double,
    val lng: Double,
    val addressFull: String?,
    val categoryId: Long,
    val avgRating: Double?,
    val coverImageUrl: String?,
    val authorId: String? = null,
    // Moderation: false while a place created by a USER waits for approval.
    val isVerified: Boolean = false,
    val status: String? = null
)

interface PlaceApiService {
    // FIX: Changed from "place/save" to "places" to match PlaceController
    @POST("places")
    suspend fun createPlace(@Body request: PlaceRequest): PlaceResponse

    // Without userEmail the server returns approved places only; with it, the
    // caller also sees their own pending ones.
    @GET("places")
    suspend fun getAllPlaces(@Query("userEmail") userEmail: String? = null): List<PlaceResponse>

    @DELETE("places/{id}")
    suspend fun deletePlace(
        @Path("id") id: Long,
        @Query("userEmail") userEmail: String
    ): Response<ResponseBody>
}