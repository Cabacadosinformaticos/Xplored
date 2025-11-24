package pt.iade.ei.xplored.data.models.users

import pt.iade.ei.xplored.data.models.misc.TimestampString
import pt.iade.ei.xplored.data.models.misc.IdString

/**
 * Mirrors table: password_reset_tokens
 */
data class PasswordResetToken(
    val tokenId: IdString,
    val userId: IdString,
    val token: String,              // 64-char
    val expiresAt: TimestampString, // DATETIME
    val usedAt: TimestampString? = null,
    val createdAt: TimestampString
)
