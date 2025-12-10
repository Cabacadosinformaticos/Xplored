package pt.iade.ei.xplored.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Moderation endpoints, restricted to the ADMIN role by the server.
interface AdminApiService {
    @GET("admin/places/pending")
    suspend fun getPendingPlaces(@Query("adminEmail") adminEmail: String): List<PlaceResponse>

    @POST("admin/places/{id}/approve")
    suspend fun approvePlace(
        @Path("id") id: Long,
        @Query("adminEmail") adminEmail: String
    ): Response<ResponseBody>

    @POST("admin/places/{id}/reject")
    suspend fun rejectPlace(
        @Path("id") id: Long,
        @Query("adminEmail") adminEmail: String
    ): Response<ResponseBody>
}
