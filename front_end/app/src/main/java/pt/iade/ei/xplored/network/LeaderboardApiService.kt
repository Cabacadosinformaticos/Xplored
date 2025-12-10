package pt.iade.ei.xplored.network

import retrofit2.http.GET
import retrofit2.http.Query

// One row of the explorers ranking.
data class LeaderboardEntry(
    val position: Int,
    val name: String,
    val points: Int,
    val profilePhoto: String? = null
)

// The caller's own position, or null when the caller is not a ranked explorer.
data class LeaderboardMe(
    val position: Int,
    val name: String,
    val points: Int
)

data class LeaderboardResponse(
    val top: List<LeaderboardEntry> = emptyList(),
    val me: LeaderboardMe? = null
)

interface LeaderboardApiService {
    // Top 10 explorers by points; `me` carries the caller's own position.
    @GET("user/leaderboard")
    suspend fun getLeaderboard(@Query("email") email: String): LeaderboardResponse
}
