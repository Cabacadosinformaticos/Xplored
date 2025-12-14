// pt.iade.ei.xplored.network.PlaceRequest

package pt.iade.ei.xplored.network


import com.google.gson.annotations.SerializedName

data class PlaceRequest(
    val name: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    @SerializedName("addressFull") val addressFull: String,
    @SerializedName("postalCode") val postalCode: String = "",
    @SerializedName("categoryId") val categoryId: Long,

    val authorId: String,

    val status: String = "APPROVED"
)