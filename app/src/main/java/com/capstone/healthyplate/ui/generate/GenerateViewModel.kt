package com.capstone.healthyplate.ui.generate

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.capstone.healthyplate.Result
import com.capstone.healthyplate.model.GeneratedRecipeList
import com.capstone.healthyplate.reftrofit.ApiConfig
import com.capstone.healthyplate.reftrofit.ApiService
import com.capstone.healthyplate.response.GenerateResponse
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MultipartBody

class GenerateViewModel(): ViewModel() {
    private lateinit var recipeList: ArrayList<GeneratedRecipeList>
    private lateinit var db: FirebaseFirestore
    private lateinit var apiService: ApiService

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

//    private var _recipeRV = MutableLiveData<ArrayList<GeneratedRecipeList>>()
//    var recipeRV: LiveData<ArrayList<GeneratedRecipeList>> = _recipeRV

    fun uploadPhoto(photo: MultipartBody.Part) : LiveData<Result<GenerateResponse>> = liveData {
        apiService = ApiConfig.getApiService()
        emit(Result.Loading)
        try {
            val result = apiService.uploadPhoto(photo)
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