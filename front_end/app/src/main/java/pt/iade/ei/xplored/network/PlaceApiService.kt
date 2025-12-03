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
    val authorId: String? = null
)

interface PlaceApiService {
    // FIX: Changed from "place/save" to "places" to match PlaceController
    @POST("places")
    suspend fun createPlace(@Body request: PlaceRequest): PlaceResponse

    // FIX: Changed from "place/get-all" to "places" to match PlaceController
    @GET("places")
    suspend fun getAllPlaces(): List<PlaceResponse>

    @DELETE("places/{id}")
    suspend fun deletePlace(
        @Path("id") id: Long,
        @Query("userEmail") userEmail: String
    ): Response<ResponseBody>
}