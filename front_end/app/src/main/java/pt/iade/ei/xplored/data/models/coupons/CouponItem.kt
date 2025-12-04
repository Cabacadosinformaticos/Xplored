package pt.iade.ei.xplored.data.models.coupons

import pt.iade.ei.xplored.data.models.misc.DateString
import pt.iade.ei.xplored.data.models.misc.IdString
import pt.iade.ei.xplored.data.models.misc.TimestampString
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.CodeType
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.DiscountType

/**
 * Mirrors table: coupons
 */
data class Coupon(
    val couponId: IdString,
    val businessId: IdString,
    val placeId: IdString? = null,
    val title: String,
    val discountType: DiscountType,
    val discountValue: Double,    // DECIMAL(7,2)
    val costPoints: Int = 0,
    val codeType: CodeType = CodeType.QR,
    val maxUsesPerUser: Int? = null,
    val expiresAt: DateString? = null,
    val terms: String? = null,
    val active: Boolean = true,
    val createdAt: TimestampString
)
