package pt.iade.ei.xplored.models

import com.google.android.gms.maps.model.LatLng
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.util.UUID

// --- Custom Type Adapter for LatLng only (Uri adapters removed) ---

class LatLngAdapter : TypeAdapter<LatLng>() {
    override fun write(out: JsonWriter, value: LatLng) {
        out.beginObject()
        out.name("latitude").value(value.latitude)
        out.name("longitude").value(value.longitude)
        out.endObject()
    }

    override fun read(reader: JsonReader): LatLng {
        var latitude = 0.0
        var longitude = 0.0
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "latitude" -> latitude = reader.nextDouble()
                "longitude" -> longitude = reader.nextDouble()
            }
        }
        reader.endObject()
        return LatLng(latitude, longitude)
    }
}

/**
 * Represents a place/post in the Xplored system.
 * NOTE: Photos are no longer embedded here.
 *       Use PhotoRepository.getByPlaceId(placeId) to load a place's photos.
 */
data class Place(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    @JsonAdapter(LatLngAdapter::class)
    val latLng: LatLng,
    val category: String,
    val authorId: String, // Links to a User
    val createdAt: String = LocalDateTime.now().toString(),
    val address: String = "",
    val rating: Double = 0.0,
    val isVerified: Boolean = false
)
