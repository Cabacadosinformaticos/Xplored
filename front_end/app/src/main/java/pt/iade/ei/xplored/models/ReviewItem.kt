package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.ReviewStatus
import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

/**
 * Mirrors table: reviews
 */
data class Review(
    val reviewId: IdString,
    val userId: IdString,
    val placeId: IdString,
    val rating: Int,              // 1..5 typically
    val title: String? = null,
    val comment: String? = null,
    val isVerifiedCustomer: Boolean = false,
    val status: ReviewStatus = ReviewStatus.PENDING,
    val createdAt: TimestampString
)
