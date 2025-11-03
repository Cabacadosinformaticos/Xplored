package pt.iade.ei.xplored.network

import pt.iade.ei.xplored.models.User
import retrofit2.http.*

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
}
