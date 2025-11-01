package pt.iade.ei.xplored.models

import java.time.LocalDateTime

/**
 * Represents a user entry in the Xplored system.
 * Mirrors both stored data and the JSON structure in SessionManager.
 */
data class User(
    val userId: Long? = null,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String? = null,
    val about: String,
    val country: String? = null,
    val points: Int = 0,
    val createdAt: String? = null
)

// Example mock user (for login tests)
val MOCK_USER = User(
    userId = 100,
    name = "cesarini",
    email = "test.pt",
    passwordHash = "1234",
    role = "basic",
    about = "test",
    country = "Portugal",
    points = 600,
    createdAt = LocalDateTime.now().toString()
)