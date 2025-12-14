package pt.iade.ei.xplored.data.models.misc

import pt.iade.ei.xplored.data.models.photos.PhotoStatus.ModerationEntityType
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.ModerationStatus

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
