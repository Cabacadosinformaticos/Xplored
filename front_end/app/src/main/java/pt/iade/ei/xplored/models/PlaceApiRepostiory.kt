package pt.iade.ei.xplored.models

import android.util.Log
import pt.iade.ei.xplored.network.ApiClient
import pt.iade.ei.xplored.network.PlaceApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PlaceApiRepository {
    private val api = ApiClient.instance.create(PlaceApiService::class.java)

    fun createPlace(place: PlaceRequest, onResult: (Boolean) -> Unit) {
        api.createPlace(place).enqueue(object : Callback<PlaceRequest> {
            override fun onResponse(call: Call<PlaceRequest>, response: Response<PlaceRequest>) {
                if (response.isSuccessful) {
                    onResult(true)
                } else {
                    Log.e("API_ERROR", "Failed: ${response.code()} ${response.errorBody()?.string()}")
                    onResult(false)
                }
            }

            override fun onFailure(call: Call<PlaceRequest>, t: Throwable) {
                Log.e("API_ERROR", "Error: ${t.message}")
                onResult(false)
            }
        })
    }

    fun fetchAllPlaces(onResult: (List<PlaceRequest>?) -> Unit) {
        api.getPlaces().enqueue(object : Callback<List<PlaceRequest>> {
            override fun onResponse(
                call: Call<List<PlaceRequest>>,
                response: Response<List<PlaceRequest>>
            ) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    Log.e("API_ERROR", "Failed: ${response.code()} ${response.errorBody()?.string()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<List<PlaceRequest>>, t: Throwable) {
                Log.e("API_ERROR", "Error: ${t.message}")
                onResult(null)
            }
        })
    }


}
