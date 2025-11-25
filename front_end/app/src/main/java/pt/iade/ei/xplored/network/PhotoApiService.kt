package pt.iade.ei.xplored.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PhotoApiService {

    @Multipart
    @POST("photos/upload")
    suspend fun uploadPhoto(
        @Part file: MultipartBody.Part,
        @Part("userId") userId: RequestBody,
        @Part("placeId") placeId: RequestBody,
        @Part("reviewId") reviewId: RequestBody
    ): PhotoResponse

    @GET("photos/by-place/{placeId}")
    suspend fun getPhotosByPlace(@Path("placeId") placeId: Long): List<PhotoResponse>
    @GET("photos/by-user/{userIdOrEmail}")
    suspend fun getPhotosByUser(@Path("userIdOrEmail") userIdOrEmail: String): List<PhotoResponse>
}