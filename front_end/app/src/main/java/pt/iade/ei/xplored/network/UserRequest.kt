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
