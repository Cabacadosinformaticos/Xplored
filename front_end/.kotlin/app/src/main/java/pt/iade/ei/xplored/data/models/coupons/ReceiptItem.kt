package pt.iade.ei.xplored.data.models.coupons

import pt.iade.ei.xplored.data.models.misc.IdString
import pt.iade.ei.xplored.data.models.misc.TimestampString
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.ReceiptStatus

/**
 * Mirrors table: receipts
 */
data class Receipt(
    val receiptId: IdString,
    val userId: IdString,
    val placeId: IdString? = null,
    val categoryId: IdString,
    val photoUrl: String,
    val status: ReceiptStatus = ReceiptStatus.PENDING,
    val pointsAwarded: Int? = null,
    val createdAt: TimestampString
)
