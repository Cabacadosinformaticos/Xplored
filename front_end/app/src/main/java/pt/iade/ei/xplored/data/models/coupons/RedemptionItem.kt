package pt.iade.ei.xplored.data.models.coupons

import pt.iade.ei.xplored.data.models.misc.TimestampString
import pt.iade.ei.xplored.data.models.misc.IdString

/**
 * Mirrors table: redemptions
 */
data class Redemption(
    val redemptionId: IdString,
    val couponId: IdString,
    val userId: IdString,
    val redeemedAt: TimestampString
)
