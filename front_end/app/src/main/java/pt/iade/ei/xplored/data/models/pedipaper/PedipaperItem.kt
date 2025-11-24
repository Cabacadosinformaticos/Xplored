package pt.iade.ei.xplored.data.models.pedipaper

import pt.iade.ei.xplored.data.models.misc.TimestampString
import pt.iade.ei.xplored.data.models.misc.IdString

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