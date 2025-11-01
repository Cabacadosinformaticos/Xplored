package pt.iade.ei.xplored.models

data class PlaceRequest(
    val name: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    val addressFull: String = "",
    val postalCode: String = "",
    val categoryId: Long = 1,
    val status: String = "pending"
)
