package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.TicketPriority
import pt.iade.ei.xplored.models.TicketStatus
import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

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
