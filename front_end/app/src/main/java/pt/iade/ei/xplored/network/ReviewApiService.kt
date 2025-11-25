package pt.iade.ei.xplored.network

import okhttp3.ResponseBody
import pt.iade.ei.xplored.data.models.reviews.Review
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApiService {

    @POST("reviews")
    suspend fun createReview(@Body request: ReviewRequest): Review

    @GET("reviews/by-place/{placeId}")
    suspend fun getReviewsByPlace(
        @Path("placeId") placeId: Long,
        @Query("userEmail") userEmail: String?
    ): List<Review>

    // --- ADD THIS ---
    @GET("reviews/by-user")
    suspend fun getReviewsByUserEmail(
        @Query("email") email: String
    ): List<Review>
    // ----------------

    @POST("reactions/toggle")
    suspend fun toggleReaction(@Body request: ReactionRequest): ResponseBody
}