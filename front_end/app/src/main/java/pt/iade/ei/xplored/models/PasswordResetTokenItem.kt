package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.TimestampString
import pt.iade.ei.xplored.models.IdString

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
