package pt.iade.ei.xplored.data.models.places

import pt.iade.ei.xplored.data.models.misc.IdString
import pt.iade.ei.xplored.data.models.misc.TimestampString
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.VisitStatus
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.VisitVerificationType

/**
 * Mirrors table: visits
 */
data class Visit(
    val visitId: IdString,
    val userId: IdString,
    val placeId: IdString,
    val verificationType: VisitVerificationType,
    val status: VisitStatus = VisitStatus.PENDING,
    val pointsEarned: Int? = null,
    val visitedAt: TimestampString,   // DATETIME
    val createdAt: TimestampString
)
