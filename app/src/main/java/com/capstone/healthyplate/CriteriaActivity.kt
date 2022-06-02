package com.capstone.healthyplate

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.healthyplate.databinding.ActivityCriteriaBinding
import com.google.firebase.auth.FirebaseAuth

class CriteriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCriteriaBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriteriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()


    }

    private fun sendEmailVerification() {
        val firebaseAuth = FirebaseAuth.getInstance()
        val firebaseUser = firebaseAuth.currentUser


        firebaseUser!!.sendEmailVerification()
            .addOnSuccessListener {
                Toast.makeText(this@CriteriaActivity, "Instructions Sent...", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@CriteriaActivity,
                    "Failed to send due to " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}