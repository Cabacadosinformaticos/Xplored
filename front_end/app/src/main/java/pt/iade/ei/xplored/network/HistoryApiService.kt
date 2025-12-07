package pt.iade.ei.xplored.network

import retrofit2.http.GET
import retrofit2.http.Query

// One movement of the user's points ledger.
data class PointsHistoryResponse(
    val amount: Int,
    val reason: String? = null,
    val createdAt: String? = null
)

interface HistoryApiService {
    // Newest first, as returned by the backend.
    @GET("user/points-history")
    suspend fun getPointsHistory(@Query("email") email: String): List<PointsHistoryResponse>
}
