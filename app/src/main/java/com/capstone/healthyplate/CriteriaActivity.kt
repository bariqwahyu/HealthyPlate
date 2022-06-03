package com.capstone.healthyplate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.healthyplate.databinding.ActivityCriteriaBinding
import com.capstone.healthyplate.ui.main.MainActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CriteriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCriteriaBinding
    private val firebaseUser = Firebase.auth.currentUser
    private val userID = firebaseUser?.uid
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriteriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.setOnClickListener {
            addData()
        }
    }

    private fun sendEmailVerification() {
        firebaseUser!!.sendEmailVerification()
            .addOnSuccessListener {
                Toast.makeText(this@CriteriaActivity, "Instructions Sent...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@CriteriaActivity, "Failed to send due to " + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun addData() {
        val name = binding.etNameCr.text.toString()
        val age = binding.etAgeCr.text.toString()
        val gender = binding.etGenderCr.text.toString()
        val job = binding.etJobCr.text.toString()
        when {
            name.isEmpty() -> {
                binding.etNameCr.error = "Masukkan Nama"
            }
            age.isEmpty() -> {
                binding.etAgeCr.error = "Masukkan Umur"
            }
            gender.isEmpty() -> {
                binding.etGenderCr.error = "Masukkan Jenis Kelamin"
            }
            job.isEmpty() -> {
                binding.etJobCr.error = "Masukkan Pekerjaan Anda"
            }
            else -> {
                uploadData(name, age, gender, job)
            }
        }
    }

    private fun uploadData(name: String, age: String, gender: String, job: String) {
        val user = hashMapOf(
            "name" to name,
            "age" to age,
            "gender" to gender,
            "job" to job
        )

        db.collection("users").document(userID.toString()).set(user)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: ${userID}")
                startActivity(Intent(this, MainActivity::class.java))
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    companion object {
        private const val TAG = "CriteriaActivity"
    }
}