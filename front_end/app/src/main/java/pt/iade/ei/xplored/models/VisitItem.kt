package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.VisitStatus
import pt.iade.ei.xplored.models.VisitVerificationType
import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

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
