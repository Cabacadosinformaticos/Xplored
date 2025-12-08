package pt.iade.ei.xplored.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Body for saving a place as a favourite.
data class FavoriteRequest(
    val userEmail: String,
    val placeId: Long
)

interface FavoriteApiService {
    // 201 the first time, 200 when the place is already a favourite.
    @POST("favorites")
    suspend fun addFavorite(@Body request: FavoriteRequest): Response<ResponseBody>

    @DELETE("favorites")
    suspend fun removeFavorite(
        @Query("userEmail") userEmail: String,
        @Query("placeId") placeId: Long
    ): Response<ResponseBody>

    // Same shape as /places, so PlaceResponse can be reused as-is.
    @GET("favorites")
    suspend fun getFavorites(@Query("userEmail") userEmail: String): List<PlaceResponse>
}
