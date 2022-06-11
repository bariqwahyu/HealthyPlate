package com.capstone.healthyplate.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.healthyplate.model.RecipeRV
import com.google.firebase.firestore.FirebaseFirestore

class HomeViewModel : ViewModel() {
    private lateinit var recipeRVList: ArrayList<RecipeRV>
    private lateinit var db: FirebaseFirestore

    private var _recipeRV = MutableLiveData<ArrayList<RecipeRV>>()
    var recipeRV: LiveData<ArrayList<RecipeRV>> = _recipeRV

    init {
        getRecipeData()
    }

    private fun getRecipeData() {
        db = FirebaseFirestore.getInstance()
        db.collection("menu")
            .get()
            .addOnSuccessListener {
                recipeRVList = arrayListOf()
                recipeRVList.clear()

                for (document in it) {
                    recipeRVList.add((RecipeRV(
                        document.data["food_name"] as String,
                        document.data["foto"] as String,
                        document.data["bahan"] as String,
                        document.data["langkah"] as String
                    )))
                }
                _recipeRV.value = recipeRVList
            }
            .addOnFailureListener {
                Log.d(TAG, it.message.toString())
            }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}