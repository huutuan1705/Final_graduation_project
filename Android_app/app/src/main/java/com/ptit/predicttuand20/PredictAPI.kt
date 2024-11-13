package com.ptit.predicttuand20

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PredictAPI {
    @POST("api/predict")
    suspend fun predict(
        @Body request: Request
    ): ResponseBody
}

data class Request(
    @SerializedName("img_base64")
    val img_base64: String
)