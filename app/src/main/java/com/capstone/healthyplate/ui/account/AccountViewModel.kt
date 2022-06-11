package com.capstone.healthyplate.ui.account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.healthyplate.model.Users
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AccountViewModel : ViewModel() {
    private val user = Firebase.auth.currentUser
    private val userID = user?.uid
    private val db = Firebase.firestore

    private val _userData = MutableLiveData<Users>()
    val userData: LiveData<Users> = _userData

    init {
        getData()
    }

    private fun getData() {
        val docRef = db.collection("users").document(userID.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val imageUri = document.getString("image_url")
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val age = document.getDouble("age")
                    val intAge = age?.toInt()
                    val gender = document.getString("gender")
                    val job = document.getString("job")

                    val dataUser = Users(imageUri, name, email, intAge.toString(), gender, job)

                    _userData.value = dataUser
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    companion object {
        private const val TAG = "AccountViewModel"
    }
}