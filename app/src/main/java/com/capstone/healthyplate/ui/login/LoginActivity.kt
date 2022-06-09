package com.capstone.healthyplate.ui.login

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.capstone.healthyplate.CriteriaActivity
import com.capstone.healthyplate.R
import com.capstone.healthyplate.databinding.ActivityLoginBinding
import com.capstone.healthyplate.ui.register.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupAction()
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            emailLogin()
        }

        binding.txtRegLogin.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.btnLoginGoogle.setOnClickListener {
            googleSignIn()
        }

        binding.txtForgotLogin.setOnClickListener {
            forgotPassword()
        }
    }

    private fun forgotPassword() {
        val emailAddress = binding.etEmailLogin.text.toString()

        if (emailAddress.isEmpty()) {
            binding.etEmailLogin.error = "Input Your Email"
        } else {
            val builder = AlertDialog.Builder(this)

            builder.setTitle("Reset Password")

            builder.setMessage("Send Reset Password Email to $emailAddress")

            builder.setPositiveButton(
                "Yes") { dialog, id ->
                auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                        }
                    }
            }

            builder.setNegativeButton(
                "Cancel") { dialog, id ->
                dialog.cancel()
            }
            builder.show()
        }
    }

    private fun emailLogin() {
        val email = binding.etEmailLogin.text.toString()
        val password = binding.etPasswordLogin.text.toString()
        when {
            email.isEmpty() -> {
                binding.etEmailLogin.error = "Input Your Email"
            }
            password.isEmpty() -> {
                binding.etPasswordLogin.error = "Input Your Password"
            }
            else -> {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
            }
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null){
            startActivity(Intent(this, CriteriaActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}