package pt.iade.ei.xplored.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

/**
 * Handles the persistence of places using SharedPreferences.
 *
 * Note: This now runs a one-time migration that moves any legacy
 * `photoUris` embedded in Place JSON into the PhotoRepository as
 * PhotoItem rows (kind = GALLERY), then strips the legacy field.
 */
object PlacePersistence {
    private const val PREFS_NAME = "XploredPlacePrefs"
    private const val KEY_PLACES = "places"

    /**
     * Saves the list of places to SharedPreferences.
     *
     * @param context The application context.
     * @param places The list of places to save.
     */
    fun savePlaces(context: Context, places: List<Place>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            val gson = Gson()
            val json = gson.toJson(places)
            putString(KEY_PLACES, json)
        }
    }

    /**
     * Loads the list of places from SharedPreferences.
     *
     * Also migrates any legacy `photoUris` arrays into PhotoRepository
     * (as PhotoItem rows) and removes that field from stored JSON so
     * migration happens only once.
     *
     * @param context The application context.
     * @return The list of saved places, or an empty list if none are found.
     */
    fun loadPlaces(context: Context): MutableList<Place> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val raw = prefs.getString(KEY_PLACES, null) ?: return mutableListOf()

        // Run one-time migration for any embedded photoUris
        val cleanedJson = migrateLegacyPhotoUrisToRepository(context, raw).also { updated ->
            if (updated != raw) {
                // Persist the cleaned JSON so we don't migrate again
                prefs.edit { putString(KEY_PLACES, updated) }
            }
        }

        val type = object : TypeToken<MutableList<Place>>() {}.type
        return gson.fromJson(cleanedJson, type) ?: mutableListOf()
    }

    /**
     * Migrates any legacy `photoUris` (array of stringified URIs) found
     * inside the stored Place JSON into PhotoRepository as rows:
     *   - reviewId = "PLACE-<placeId>"
     *   - placeId  = <placeId>
     *   - userId   = <authorId>
     *   - url      = <element of photoUris[]>
     *   - status   = APPROVED
     *   - kind     = GALLERY
     *
     * Returns the updated JSON string with the `photoUris` field removed.
     * If nothing to migrate, returns the original string unchanged.
     */
    private fun migrateLegacyPhotoUrisToRepository(context: Context, json: String): String {
        val arr = try { JSONArray(json) } catch (_: Throwable) { return json }
        var mutated = false

        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue

            val placeId = obj.optString("id", "")
            val authorId = obj.optString("authorId", "")
            val legacy = obj.optJSONArray("photoUris") ?: continue
            if (placeId.isBlank()) continue

            // Move each legacy photo into the centralized PhotoRepository
            for (j in 0 until legacy.length()) {
                val url = legacy.optString(j, null) ?: continue
                PhotoRepository.insert(
                    context,
                    PhotoItem(
                        reviewId = "PLACE-$placeId",
                        placeId = placeId,
                        userId = authorId,
                        url = url,
                        status = PhotoStatus.APPROVED,
                        kind = PhotoKind.GALLERY
                    )
                )
            }

            // Remove the legacy field and mark as mutated
            obj.remove("photoUris")
            mutated = true
        }

        return if (mutated) arr.toString() else json
    }
}
