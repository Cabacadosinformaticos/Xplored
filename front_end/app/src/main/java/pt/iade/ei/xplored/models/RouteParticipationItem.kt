package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

/**
 * Mirrors table: route_participations
 */
data class RouteParticipation(
    val participationId: IdString,
    val routeId: IdString,
    val userId: IdString,
    val startedAt: TimestampString,
    val completedAt: TimestampString? = null,
    val pointsAwarded: Int? = null
)
