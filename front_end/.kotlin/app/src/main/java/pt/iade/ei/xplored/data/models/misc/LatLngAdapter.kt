package pt.iade.ei.xplored.data.models.misc

import com.google.android.gms.maps.model.LatLng
import com.google.gson.*
import java.lang.reflect.Type

class LatLngAdapter : JsonSerializer<LatLng>, JsonDeserializer<LatLng> {

    override fun serialize(
        src: LatLng,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val obj = JsonObject()
        obj.addProperty("lat", src.latitude)
        obj.addProperty("lng", src.longitude)
        return obj
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LatLng {

        // Case 1: json is null or not an object
        if (!json.isJsonObject) {
            return LatLng(0.0, 0.0)
        }

        val obj = json.asJsonObject

        // Case 2: New format { "lat": ..., "lng": ... }
        val hasLat = obj.has("lat") && !obj.get("lat").isJsonNull
        val hasLng = obj.has("lng") && !obj.get("lng").isJsonNull
        if (hasLat && hasLng) {
            return LatLng(
                obj.get("lat").asDouble,
                obj.get("lng").asDouble
            )
        }

        // Case 3: Legacy Google format { "latitude": ..., "longitude": ... }
        val hasLatitude = obj.has("latitude") && !obj.get("latitude").isJsonNull
        val hasLongitude = obj.has("longitude") && !obj.get("longitude").isJsonNull
        if (hasLatitude && hasLongitude) {
            return LatLng(
                obj.get("latitude").asDouble,
                obj.get("longitude").asDouble
            )
        }

        // Case 4: Legacy string: "38.7223,-9.1393"
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            val parts = json.asString.split(",")
            if (parts.size == 2) {
                return LatLng(
                    parts[0].toDoubleOrNull() ?: 0.0,
                    parts[1].toDoubleOrNull() ?: 0.0
                )
            }
        }

        // Case 5: Missing or corrupted data
        return LatLng(0.0, 0.0)
    }
}
