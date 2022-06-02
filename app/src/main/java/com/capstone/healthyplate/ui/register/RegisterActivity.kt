package com.capstone.healthyplate.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.healthyplate.databinding.ActivityRegisterBinding
import com.capstone.healthyplate.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        setupAction()
    }

    private fun setupAction() {
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
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            startActivity(Intent(this, LoginActivity::class.java))
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.txtLoginReg.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}