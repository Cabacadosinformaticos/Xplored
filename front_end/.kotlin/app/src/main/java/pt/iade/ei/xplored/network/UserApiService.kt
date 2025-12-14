package pt.iade.ei.xplored.network

import pt.iade.ei.xplored.data.models.users.User
import retrofit2.http.*
import okhttp3.ResponseBody
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserApiService {
    @GET("user/get-all")
    suspend fun getUsers(): List<User>

    @POST("user/save")
    suspend fun createUser(@Body user: User): User

    @GET("user/login")
    suspend fun getUserByEmailAndPassword(
        @Query("email") email: String,
        @Query("password") password: String
    ): User?

    @PUT("user/update-points")
    suspend fun updatePoints(
        @Query("email") email: String,
        @Query("points") points: Int
    ): ResponseBody

    // --- FIX: Using FormUrlEncoded is safer for text blocks (About Me) ---
    @FormUrlEncoded
    @PUT("user/update-profile")
    suspend fun updateProfile(
        @Field("email") email: String,
        @Field("name") name: String,
        @Field("about") about: String,
        @Field("country") country: String
    ): User

    @GET("user/by-email")
    suspend fun getUserByEmail(@Query("email") email: String): User
}