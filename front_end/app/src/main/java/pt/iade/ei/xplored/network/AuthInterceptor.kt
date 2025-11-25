package pt.iade.ei.xplored.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import pt.iade.ei.xplored.SessionManager

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = SessionManager.getToken(context)

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
