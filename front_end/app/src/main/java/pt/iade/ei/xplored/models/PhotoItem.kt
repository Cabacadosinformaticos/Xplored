package pt.iade.ei.xplored.models

import java.time.LocalDateTime
import java.util.UUID

data class PhotoItem(
    val photoId: String = UUID.randomUUID().toString(),
    val reviewId: String,
    val placeId: String? = null,
    val userId: String,
    val url: String,
    val status: PhotoStatus = PhotoStatus.PENDING,  // from EnumsItem.kt
    val createdAt: String = LocalDateTime.now().toString(),
    val kind: PhotoKind = PhotoKind.GALLERY        // from EnumsItem.kt
)
