package com.capstone.healthyplate.ui.generate

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.capstone.healthyplate.Result
import com.capstone.healthyplate.reftrofit.ApiConfig
import com.capstone.healthyplate.reftrofit.ApiService
import com.capstone.healthyplate.response.GenerateResponse
import okhttp3.MultipartBody

class GenerateViewModel(): ViewModel() {
    private lateinit var apiService: ApiService

    fun uploadPhoto(file: MultipartBody.Part) : LiveData<Result<GenerateResponse>> = liveData {
        apiService = ApiConfig.getApiService()
        emit(Result.Loading)
        try {
            val result = apiService.uploadPhoto(file)
            emit(Result.Success(result))
        } catch (e: Exception) {
            Log.d(TAG, "Generate Recipe: ${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
    }

    companion object {
        private const val TAG = "GenerateViewModel"
    }
}