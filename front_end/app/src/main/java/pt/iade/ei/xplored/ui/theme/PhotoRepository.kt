// PhotoRepository.kt
package pt.iade.ei.xplored.models

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

object PhotoRepository {
    private const val PREF_PHOTOS = "XploredPhotos"
    private const val KEY_PHOTOS = "photos"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREF_PHOTOS, Context.MODE_PRIVATE)

    private fun loadArray(ctx: Context): JSONArray {
        val raw = prefs(ctx).getString(KEY_PHOTOS, "[]") ?: "[]"
        return JSONArray(raw)
    }

    private fun saveArray(ctx: Context, arr: JSONArray) {
        prefs(ctx).edit { putString(KEY_PHOTOS, arr.toString()) }
    }

    fun insert(ctx: Context, photo: PhotoItem) {
        val arr = loadArray(ctx)
        arr.put(photo.toJson())
        saveArray(ctx, arr)
    }

    /** Only gallery photos for a user (excludes AVATAR). */
    fun getGalleryByUserId(ctx: Context, userId: String): List<PhotoItem> {
        val arr = loadArray(ctx)
        val out = mutableListOf<PhotoItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("userId") == userId &&
                o.optString("kind") == PhotoKind.GALLERY.name) {
                out += o.toPhoto()
            }
        }
        return out
    }

    fun getByPlaceId(ctx: Context, placeId: String): List<PhotoItem> {
        val arr = loadArray(ctx)
        val out = mutableListOf<PhotoItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("placeId") == placeId) out += o.toPhoto()
        }
        return out
    }

    fun getByReviewId(ctx: Context, reviewId: String): List<PhotoItem> {
        val arr = loadArray(ctx)
        val out = mutableListOf<PhotoItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("reviewId") == reviewId) out += o.toPhoto()
        }
        return out
    }

    /** Fetch current avatar for user (kind=AVATAR). */
    fun getAvatarByUserId(ctx: Context, userId: String): PhotoItem? {
        val arr = loadArray(ctx)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("userId") == userId &&
                o.optString("kind") == PhotoKind.AVATAR.name) {
                return o.toPhoto()
            }
        }
        return null
    }

    /** Replace existing avatar row (if any) with a new one. */
    fun upsertAvatar(ctx: Context, userId: String, url: String) {
        val arr = loadArray(ctx)

        // remove any existing AVATAR rows for this user
        val toKeep = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val isThisUsersAvatar =
                o.optString("userId") == userId &&
                        o.optString("kind") == PhotoKind.AVATAR.name
            if (!isThisUsersAvatar) toKeep.put(o)
        }

        // add new avatar row
        toKeep.put(
            PhotoItem(
                reviewId = "AVATAR-$userId",
                placeId = null,
                userId = userId,
                url = url,
                status = PhotoStatus.APPROVED,
                kind = PhotoKind.AVATAR
            ).toJson()
        )
        saveArray(ctx, toKeep)
    }

    // ---- private helpers ----
    private fun PhotoItem.toJson(): JSONObject = JSONObject().apply {
        put("photoId", photoId)
        put("reviewId", reviewId)
        put("placeId", placeId)
        put("userId", userId)
        put("url", url)
        put("status", status.name)
        put("createdAt", createdAt)
        put("kind", kind.name)
    }

    private fun JSONObject.toPhoto(): PhotoItem =
        PhotoItem(
            photoId   = getString("photoId"),
            reviewId  = getString("reviewId"),
            placeId   = optString("placeId").takeIf { it.isNotBlank() },
            userId    = getString("userId"),
            url       = getString("url"),
            status    = PhotoStatus.valueOf(getString("status")),
            createdAt = getString("createdAt"),
            kind      = PhotoKind.valueOf(optString("kind", PhotoKind.GALLERY.name))
        )
}
