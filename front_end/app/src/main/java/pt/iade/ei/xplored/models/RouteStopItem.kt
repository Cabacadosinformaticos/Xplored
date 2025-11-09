package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.IdString

/**
 * Mirrors table: route_stops
 */
data class RouteStop(
    val stopId: IdString,
    val routeId: IdString,
    val placeId: IdString,
    val orderNum: Int,
    val requiresPhoto: Boolean = false,
    val taskDescription: String? = null
)
