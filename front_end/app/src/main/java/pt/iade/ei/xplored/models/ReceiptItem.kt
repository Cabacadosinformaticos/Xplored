package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.ReceiptStatus
import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

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
