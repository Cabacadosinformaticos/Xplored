package pt.iade.ei.xplored.data.models.users

import pt.iade.ei.xplored.data.models.misc.IdString
import pt.iade.ei.xplored.data.models.photos.PhotoStatus.PrivacyLevel

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
