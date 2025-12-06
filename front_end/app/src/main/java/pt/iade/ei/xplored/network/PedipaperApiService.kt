package pt.iade.ei.xplored.network

import okhttp3.ResponseBody
import retrofit2.http.*

// 1. Model for the Route List
data class Pedipaper(
    val pediId: Long,
    val name: String,
    val description: String?,
    val totalPoints: Int,
    val active: Boolean
)

// 2. Model for the Stops
data class StopResponse(
    val stopId: Long,
    val orderNum: Int,
    val taskDescription: String?,
    val requiresPhoto: Boolean,
    val placeId: Long,
    val placeName: String,
    val lat: Double,
    val lng: Double,
    val placeCoverUrl: String?
)

// 3. Check-in request: current GPS position and optional evidence photo
data class CheckinRequest(
    val userEmail: String,
    val lat: Double,
    val lng: Double,
    val photoId: Long? = null
)

// 4. Check-in response: how many stops are done out of the total
data class CheckinResponse(
    val completedStops: Int,
    val totalStops: Int
)

// 5. Server-side progress on a route
data class RouteProgress(
    val joined: Boolean,
    val completed: Boolean,
    val completedStopIds: List<Long>,
    val totalStops: Int,
    val pointsAwarded: Int
)

// 6. Completion response: points awarded and the fresh balance
data class CompleteResponse(
    val pointsAwarded: Int,
    val newBalance: Int
)

interface PedipaperApiService {
    @GET("pedipapers")
    suspend fun getAllPedipapers(): List<Pedipaper>

    @GET("pedipapers/{id}/stops")
    suspend fun getStops(@Path("id") id: Long): List<StopResponse>

    // FIX: Change 'Any' to 'ResponseBody' to handle plain text responses
    @POST("pedipapers/{id}/join")
    suspend fun joinPedipaper(
        @Path("id") id: Long,
        @Query("userEmail") userEmail: String
    ): ResponseBody

    // Register a GPS check-in at one stop (photoId only for stops that require it)
    @POST("pedipapers/{id}/stops/{stopId}/checkin")
    suspend fun checkinStop(
        @Path("id") id: Long,
        @Path("stopId") stopId: Long,
        @Body body: CheckinRequest
    ): CheckinResponse

    @GET("pedipapers/{id}/progress")
    suspend fun getProgress(
        @Path("id") id: Long,
        @Query("userEmail") userEmail: String
    ): RouteProgress

    @POST("pedipapers/{id}/complete")
    suspend fun completePedipaper(
        @Path("id") id: Long,
        @Query("userEmail") userEmail: String
    ): CompleteResponse
}
