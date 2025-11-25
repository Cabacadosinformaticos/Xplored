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

    // FIX: Change 'Any' to 'ResponseBody'
    @POST("pedipapers/{id}/complete")
    suspend fun completePedipaper(
        @Path("id") id: Long,
        @Query("userEmail") userEmail: String
    ): ResponseBody
}