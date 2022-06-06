package com.capstone.healthyplate.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.healthyplate.databinding.ActivityRegisterBinding
import com.capstone.healthyplate.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        createAccount()
    }

    private fun createAccount() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmailReg.text.toString()
            val password = binding.etPasswordReg.text.toString()
            val confirmPassword = binding.etConfirmPasswordReg.text.toString()
            when {
                email.isEmpty() -> {
                    binding.etEmailReg.error = "Masukkan email"
                }
                password.isEmpty() -> {
                    binding.etPasswordReg.error = "Masukkan password"
                }
                confirmPassword.isEmpty() -> {
                    binding.etConfirmPasswordReg.error = "Masukkan password"
                }
                confirmPassword != password -> {
                    binding.etConfirmPasswordReg.error = "Password tidak sesuai"
                }
                else -> {
                    auth.createUserWithEmailAndPassword(email, confirmPassword)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "createUserWithEmail:success")
                                startActivity(Intent(this, LoginActivity::class.java))
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

        binding.txtLoginReg.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    companion object {
        private const val TAG = "RegisterActivity"
    }
}