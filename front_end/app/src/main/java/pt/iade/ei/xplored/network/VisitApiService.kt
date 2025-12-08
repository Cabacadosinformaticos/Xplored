package pt.iade.ei.xplored.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// GPS check-in at a place ("Estive aqui").
data class VisitRequest(
    val userEmail: String,
    val placeId: Long,
    val lat: Double,
    val lng: Double
)

data class VisitResponse(
    val pointsAwarded: Int,
    val newBalance: Int,
    val visitCount: Int
)

data class VisitCountResponse(
    val visitCount: Int
)

interface VisitApiService {
    // 201 with +10 points the first time, 200 with 0 points if already visited.
    @POST("visits")
    suspend fun checkin(@Body request: VisitRequest): Response<VisitResponse>

    @GET("visits/count")
    suspend fun getVisitCount(@Query("userEmail") userEmail: String): VisitCountResponse
}
