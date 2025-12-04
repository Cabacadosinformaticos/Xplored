package pt.iade.ei.xplored.data.models.users

/**
 * Represents a user in the Xplored system.
 * Matches the JSON returned by the backend: id, name, email, about, country,
 * role, profilePhoto, points, createdAt. Passwords are never serialised.
 */
data class User(
    val id: Long? = null,
    val name: String,
    val email: String,
    val about: String? = null,
    val country: String? = null,
    val role: String? = null,
    val profilePhoto: String? = null,
    val points: Int = 0,
    val createdAt: String? = null
)
