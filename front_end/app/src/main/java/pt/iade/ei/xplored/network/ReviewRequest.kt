package pt.iade.ei.xplored.network

data class ReviewRequest(
    val userEmail: String, // Changed from userId: Long
    val placeId: Long,
    val rating: Int,
    val title: String,
    val comment: String,
    val isVerifiedCustomer: Boolean = false,
    val status: String = "APPROVED"
)