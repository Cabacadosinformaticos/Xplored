package pt.iade.ei.xplored.network

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object PhotoUploadClient {

    private const val TAG = "PhotoUploadClient"

    private fun textPart(value: String): RequestBody =
        value.toRequestBody(null)

    suspend fun uploadPhoto(
        context: Context,
        uri: Uri,
        userId: String,
        placeId: String? = "",
        reviewId: String? = ""
    ): PhotoResponse? {
        return try {
            val resolver = context.contentResolver
            val inputStream = resolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot open input stream for URI: $uri")

            val bytes = inputStream.readBytes()
            inputStream.close()

            val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())

            // "file" matches @RequestParam("file") in PhotoController.java
            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = "xplored_${System.currentTimeMillis()}.jpg",
                body = requestFile
            )

            // FIX: Access the Singleton instance property correctly
            val api = ApiClient.instance.create(PhotoApiService::class.java)

            val response = api.uploadPhoto(
                file = filePart,
                userId = textPart(userId),
                placeId = textPart(placeId ?: ""),
                reviewId = textPart(reviewId ?: "")
            )

            Log.d(TAG, "Upload Success: ${response.url}")
            response

        } catch (e: Exception) {
            Log.e(TAG, "Upload failed for $uri: ${e.message}", e)
            null
        }
    }
}