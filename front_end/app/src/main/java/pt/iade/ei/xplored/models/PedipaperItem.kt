package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

/**
 * Mirrors table: pedipapers
 */
data class Pedipaper(
    val routeId: IdString,
    val name: String,
    val description: String? = null,
    val totalPoints: Int = 0,
    val active: Boolean = true,
    val createdAt: TimestampString
)