package pt.iade.ei.xplored.data.models.pedipaper

import pt.iade.ei.xplored.data.models.misc.TimestampString
import pt.iade.ei.xplored.data.models.misc.IdString

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
