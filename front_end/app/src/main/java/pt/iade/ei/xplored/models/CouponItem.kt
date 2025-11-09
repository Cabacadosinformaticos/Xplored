package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.CodeType
import pt.iade.ei.xplored.models.DiscountType
import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString
import pt.iade.ei.xplored.models.DateString

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
val MOCK_COUPONS: List<Coupon> = listOf(
    // 1) Classic % discount
    Coupon(
        couponId = "CPN-0001",
        businessId = "USR-0002",              // Maria Santos (BUSINESS) from MOCK_USERS
        placeId = "PLC-0001",
        title = "10% Off Any Meal",
        discountType = DiscountType.PERCENT,
        discountValue = 10.0,                 // percent
        costPoints = 250,
        codeType = CodeType.QR,
        maxUsesPerUser = 1,
        expiresAt = "2025-12-31",             // DateString (YYYY-MM-DD)
        terms = "Show QR at checkout. Not combinable with other offers.",
        active = true,
        createdAt = "2025-10-20T10:00:00Z"
    ),

    // 2) Fixed € value
    Coupon(
        couponId = "CPN-0002",
        businessId = "USR-0002",
        placeId = "PLC-0002",
        title = "€5 Coffee Bundle",
        discountType = DiscountType.FIXED,
        discountValue = 5.00,                 // euros off
        costPoints = 150,
        codeType = CodeType.QR,
        maxUsesPerUser = 2,
        expiresAt = "2026-01-31",
        terms = "Minimum spend €10. Dine-in or takeaway.",
        active = true,
        createdAt = "2025-10-20T10:00:00Z"
    ),

    // 3) “2-for-1” styled (modeled as 50% off the pair)
    Coupon(
        couponId = "CPN-0003",
        businessId = "USR-0002",
        placeId = "PLC-0003",
        title = "Oferta 2 por 1*",
        discountType = DiscountType.PERCENT,
        discountValue = 50.0,                 // models 2-for-1 as 50% off the pair
        costPoints = 120,
        codeType = CodeType.QR,
        maxUsesPerUser = 1,
        expiresAt = "2026-03-31",
        terms = "Valid for pastries only. Lowest-priced item discounted.",
        active = true,
        createdAt = "2025-10-20T10:00:00Z"
    )
)
