package com.capstone.healthyplate.ui.generate

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.capstone.healthyplate.Result
import com.capstone.healthyplate.reftrofit.ApiService
import com.capstone.healthyplate.response.GenerateResponse
import okhttp3.MultipartBody

class GenerateViewModel(private val apiService: ApiService): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun uploadPhoto(photo: MultipartBody.Part) : LiveData<Result<GenerateResponse>> = liveData {
        emit(Result.Loading)
        try {
            val result = apiService.uploadPhoto(photo)
            emit(Result.Success(result))
        } catch (e: Exception) {
            Log.d("StoryRepository", "addStories: ${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
    }
}