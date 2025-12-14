package pt.iade.ei.xplored.data.models.places

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import pt.iade.ei.xplored.data.models.misc.LatLngAdapter
import java.util.UUID

data class Place(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    @JsonAdapter(LatLngAdapter::class)
    val latLng: LatLng,
    val category: String,
    val authorId: String,
    val address: String,
    val rating: Double,
    val isVerified: Boolean = false,
    @SerializedName("photoUris")
    val photoUris: List<String> = emptyList()
)
