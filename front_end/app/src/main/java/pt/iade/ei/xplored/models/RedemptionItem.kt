package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

/**
 * Mirrors table: redemptions
 */
data class Redemption(
    val redemptionId: IdString,
    val couponId: IdString,
    val userId: IdString,
    val redeemedAt: TimestampString
)
