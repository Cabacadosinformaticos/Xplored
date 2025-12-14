package pt.iade.ei.xplored.data.models.support

import pt.iade.ei.xplored.data.models.misc.IdString
import pt.iade.ei.xplored.data.models.misc.TimestampString
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.TicketPriority
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.TicketStatus

/**
 * Mirrors table: support_tickets
 */
data class SupportTicket(
    val ticketId: IdString,
    val userId: IdString,
    val subject: String,
    val message: String,
    val status: TicketStatus = TicketStatus.OPEN,
    val priority: TicketPriority = TicketPriority.LOW,
    val createdAt: TimestampString,
    val closedAt: TimestampString? = null
)
