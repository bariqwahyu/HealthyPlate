package com.capstone.healthyplate.reftrofit

import com.capstone.healthyplate.response.GenerateResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("predict")
    suspend fun uploadPhoto(
        @Part file: MultipartBody.Part
    ): GenerateResponse
}