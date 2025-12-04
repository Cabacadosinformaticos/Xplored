package pt.iade.ei.xplored.network

import pt.iade.ei.xplored.data.models.users.User
import retrofit2.http.*

interface UserApiService {
    /** 200 with the User, or 401 when the credentials are wrong. */
    @POST("user/login")
    suspend fun login(@Body body: LoginRequest): User

    /** 201 with the created User, 409 when the email already exists. */
    @POST("user/register")
    suspend fun register(@Body body: RegisterRequest): User

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
