package com.capstone.healthyplate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.healthyplate.databinding.ActivityCriteriaBinding
import com.capstone.healthyplate.ui.main.MainActivity
import com.capstone.healthyplate.ui.welcome.WelcomeActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CriteriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCriteriaBinding
    private val user = Firebase.auth.currentUser
    private val userID = user?.uid
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriteriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCriteria()
        binding.btnNext.setOnClickListener {
            addData()
        }
    }

    private fun sendEmailVerification() {
        user!!.sendEmailVerification()
            .addOnSuccessListener {
                Toast.makeText(this@CriteriaActivity, "Instructions Sent...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@CriteriaActivity, "Failed to send due to " + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkCriteria() {
        val docRef = db.collection("users").document(userID.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val email = document.getString("email")
                    val name = document.getString("name")
                    val age = document.getString("age")
                    val gender = document.getString("gender")
                    val job = document.getString("job")
                    if (email != null && name != null && age != null && gender != null && job != null) {
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        binding.apply {
                            etNameCr.setText(name)
                            etAgeCr.setText(age)
                            etGenderCr.setText(gender)
                            etJobCr.setText(job)
                        }
                    }
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    getUserData()
                    getUserDataFromProvider()
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun getUserData() {
        user?.let {
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl
            val emailVerified = user.isEmailVerified
            val uid = user.uid
            if (name != null) {
                binding.etNameCr.setText(name)
            }
        }
    }

    private fun getUserDataFromProvider() {
        user?.let {
            for (profile in it.providerData) {
                val providerId = profile.providerId
                val uid = profile.uid
                val name = profile.displayName
                val email = profile.email
                val photoUrl = profile.photoUrl
                if (name != null) {
                    binding.etNameCr.setText(name)
                }
            }
        }
    }

    private fun addData() {
        val email = user?.email.toString()
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
                uploadData(email, name, age, gender, job)
            }
        }
    }

    private fun uploadData(email: String, name: String, age: String, gender: String, job: String) {
        val user = hashMapOf(
            "email" to email,
            "name" to name,
            "age" to age,
            "gender" to gender,
            "job" to job
        )

        db.collection("users").document(userID.toString()).set(user)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added with ID: $userID")
                startActivity(Intent(this, MainActivity::class.java))
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                signOut()
                true
            }
            else -> true
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    companion object {
        private const val TAG = "CriteriaActivity"
    }
}