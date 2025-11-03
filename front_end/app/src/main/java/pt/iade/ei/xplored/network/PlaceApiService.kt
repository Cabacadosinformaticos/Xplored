package pt.iade.ei.xplored.network

import pt.iade.ei.xplored.models.PlaceRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PlaceApiService {

    @GET("places")
    fun getPlaces(): Call<List<PlaceRequest>>


    @POST("places")
    fun createPlace(@Body place: PlaceRequest): Call<PlaceRequest>
}
