package pt.iade.ei.xplored.network

/** Body for POST /user/login. */
data class LoginRequest(
    val email: String,
    val password: String
)

/** Body for POST /user/register. */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

/** Body for POST /user/password-reset/request. */
data class PasswordResetRequest(
    val email: String
)

/** Body for POST /user/password-reset/confirm. */
data class PasswordResetConfirmRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

/** Response of the password-reset endpoints: {message} on success, {error} on 400. */
data class MessageResponse(
    val message: String? = null,
    val error: String? = null
)
