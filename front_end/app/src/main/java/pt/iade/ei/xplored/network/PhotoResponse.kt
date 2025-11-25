package pt.iade.ei.xplored.network

data class PhotoResponse(
    val photoId: Long?,
    val reviewId: String?,
    val placeId: String?,
    val userId: String?,
    val url: String?,
    val status: String?,
    val createdAt: String?
)