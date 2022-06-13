package com.capstone.healthyplate.ui.login

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.capstone.healthyplate.CriteriaActivity
import com.capstone.healthyplate.R
import com.capstone.healthyplate.databinding.ActivityLoginBinding
import com.capstone.healthyplate.model.Users
import com.capstone.healthyplate.ui.main.MainActivity
import com.capstone.healthyplate.ui.register.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val db = Firebase.firestore

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
                showLoading(true)
                auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        showLoading(false)
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
                showLoading(true)
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            showLoading(false)
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            showLoading(false)
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
                showLoading(true)
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
                    showLoading(false)
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    showLoading(false)
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null){
            checkCriteria(user)
        }
    }

    private fun checkCriteria(user: FirebaseUser?) {
        showLoading(true)
        val userID = user!!.uid
        val usersRef = db.collection("users").document(userID)
        usersRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val imageUri = document.getString("image_url")
                    val email = document.getString("email")
                    val name = document.getString("name")
                    val age = document.get("age")
                    val intAge = age?.toString()?.toInt()
                    val stringAge = intAge.toString()
                    val gender = document.getString("gender")
                    val job = document.getString("job")
                    if (imageUri != null && email != null && name != null && age != null && gender != null && job != null) {
                        showLoading(false)
                        if (stringAge.isDigitsOnly()) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            val dataUser = Users(imageUri, name, email, stringAge, gender, job)
                            val intent = Intent(this, CriteriaActivity::class.java)
                            intent.putExtra(CriteriaActivity.EXTRA_USER, dataUser)
                            startActivity(intent)
                        }
                    } else {
                        showLoading(false)
                        val dataUser = Users(imageUri, name, email, stringAge, gender, job)
                        val intent = Intent(this, CriteriaActivity::class.java)
                        intent.putExtra(CriteriaActivity.EXTRA_USER, dataUser)
                        startActivity(intent)
                    }
                } else {
                    showLoading(false)
                    startActivity(Intent(this, CriteriaActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                progressBarLayout.visibility = View.VISIBLE
            } else {
                progressBarLayout.visibility = View.INVISIBLE
            }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}