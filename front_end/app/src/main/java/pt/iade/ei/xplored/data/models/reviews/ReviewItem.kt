package pt.iade.ei.xplored.data.models.reviews

import com.google.gson.annotations.SerializedName

data class Review(
    val reviewId: Long,
    val userId: Long,
    val placeId: Long,
    val rating: Int,
    val title: String? = null,
    val comment: String? = null,
    val createdAt: String? = null,

    // User Info (for Place Detail screen)
    val userName: String? = null,
    val userPhotoUrl: String? = null,

    // Place Info (NEW - Required for Profile screen)
    val placeName: String? = null,
    val placeCoverUrl: String? = null,

    // Reactions
    val likesCount: Int = 0,
    val dislikesCount: Int = 0,
    val currentUserReaction: String? = null,

    // Photos
    @SerializedName("reviewPhotoUrls")
    val photoUrls: List<String> = emptyList()
)