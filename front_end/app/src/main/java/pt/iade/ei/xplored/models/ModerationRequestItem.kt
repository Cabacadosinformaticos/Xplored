package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.ModerationEntityType
import pt.iade.ei.xplored.models.ModerationStatus
import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

/**
 * Mirrors table: moderation_requests
 */
data class ModerationRequest(
    val modId: IdString,
    val entityType: ModerationEntityType,
    val entityId: IdString,
    val submittedBy: IdString,
    val reviewedBy: IdString? = null,
    val status: ModerationStatus = ModerationStatus.PENDING,
    val reason: String? = null,
    val requestedAt: TimestampString,
    val reviewedAt: TimestampString? = null
)
