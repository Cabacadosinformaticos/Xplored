package pt.iade.ei.xplored.network

import com.google.gson.annotations.SerializedName

data class PhotoResponse(
    // Lists send "photoId"; the upload response sends "id"
    @SerializedName(value = "photoId", alternate = ["id"])
    val photoId: Long?,
    val reviewId: String?,
    val placeId: String?,
    val userId: String?,
    val url: String?,
    val status: String?,
    val createdAt: String?
)