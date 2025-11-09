package pt.iade.ei.xplored.models

import pt.iade.ei.xplored.models.PrivacyLevel
import pt.iade.ei.xplored.models.IdString

/**
 * Mirrors table: user_settings
 * notificationsJson / mapPrefsJson kept as raw JSON strings for now.
 */
data class UserSettings(
    val userId: IdString,
    val privacyLevel: PrivacyLevel = PrivacyLevel.PUBLIC,
    val notificationsJson: String? = null,
    val mapPrefsJson: String? = null,
    val supportOptIn: Boolean = false
)
