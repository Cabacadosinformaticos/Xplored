package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

/**
 * Mirrors table: business_accounts
 * businessId is also a FK to users.user_id
 */
data class BusinessAccount(
    val businessId: IdString,
    val placeId: IdString,
    val businessName: String? = null,
    val description: String? = null,
    val approved: Boolean = false,
    val createdAt: TimestampString
)
