package pt.iade.ei.xplored.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class CouponResponse(
    val id: Long,
    val title: String,
    // The API hides the code until the coupon is redeemed.
    val description: String? = null,
    val code: String? = null,
    val merchant: String,
    val details: String? = null,
    val cost: Int
)

// Request Object for redeeming
data class RedemptionRequest(
    val userEmail: String,
    val couponId: Long
)

// Response Object for successful redemption
data class RedemptionSuccessResponse(
    val message: String,
    val newBalance: Int,
    val code: String? = null
)

interface CouponApiService {
    @GET("coupons/active")
    suspend fun getActiveCoupons(): List<CouponResponse>

    @POST("coupons/redeem")
    suspend fun redeemCoupon(@Body request: RedemptionRequest): Response<RedemptionSuccessResponse>
}