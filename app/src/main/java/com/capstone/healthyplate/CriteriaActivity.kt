package com.capstone.healthyplate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.capstone.healthyplate.databinding.ActivityCriteriaBinding
import com.capstone.healthyplate.ui.main.MainActivity
import com.capstone.healthyplate.ui.welcome.WelcomeActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CriteriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCriteriaBinding
    private val user = Firebase.auth.currentUser
    private val userID = user?.uid
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private var imageUri: Uri? = null
    private var userImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriteriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCriteria()

        binding.imgProfileCr.setOnClickListener {
            selectImage()
        }

        emailVerification()

        val spinner = binding.spGender
        ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

        binding.btnNext.setOnClickListener {
            uploadProfilePicture()
        }
    }

    private fun emailVerification() {
        val isVerified = ContextCompat.getDrawable(this, R.drawable.ic_check_white_24dp)
        val notVerified = ContextCompat.getDrawable(this, R.drawable.ic_close_red_24dp)
        if (user!!.isEmailVerified) {
            binding.btnVerification.setCompoundDrawables(null, null, isVerified, null)
            binding.btnVerification.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green1)
            binding.btnVerification.setText("Verified")
            Toast.makeText(this, "User is verified...", Toast.LENGTH_SHORT).show()
        } else {
            binding.btnVerification.setCompoundDrawables(null, null, notVerified, null)
            binding.btnVerification.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green1)
            binding.btnVerification.setText("Not Verified")
            binding.btnVerification.setOnClickListener {
                user.sendEmailVerification()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Instructions Sent...", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to send due to " + e.message, Toast.LENGTH_SHORT).show()
                    }
            }
            Toast.makeText(this, "User isn't verified...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCriteria() {
        val usersRef = db.collection("users").document(userID.toString())
        usersRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val imageUri = document.getString("image_url")
                    val email = document.getString("email")
                    val name = document.getString("name")
                    val age = document.get("age")
                    val gender = document.getString("gender")
                    val job = document.getString("job")
                    if (imageUri != null && email != null && name != null && age != null && gender != null && job != null) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Glide.with(this)
                            .load(imageUri)
                            .error(R.drawable.blank_profile)
                            .circleCrop()
                            .into(binding.imgProfileCr)
                        binding.apply {
                            etNameCr.setText(name)
                            etAgeCr.setText(age.toString())
                            etJobCr.setText(job)
                            if (gender == "Male" || gender == null) {
                                spGender.setSelection(0)
                            } else {
                                spGender.setSelection(1)
                            }
                        }
                    }
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun addData(imageUri: String) {
        val email = user?.email.toString()
        val name = binding.etNameCr.text.toString()
        val age = binding.etAgeCr.text.toString()
        val gender = binding.spGender.selectedItem.toString()
        val job = binding.etJobCr.text.toString()
        when {
            name.isEmpty() -> {
                binding.etNameCr.error = "Masukkan Nama"
            }
            age.isEmpty() -> {
                binding.etAgeCr.error = "Masukkan Umur"
            }
            job.isEmpty() -> {
                binding.etJobCr.error = "Masukkan Pekerjaan Anda"
            }
            else -> {
                uploadData(imageUri, email, name, age.toInt(), gender, job)
            }
        }
    }

    private fun selectImage() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, resources.getString(R.string.choose_image))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data as Uri
            binding.imgProfileCr.setImageURI(imageUri)
        }
    }

    private fun uploadProfilePicture(){
        if (imageUri == null) {
            imageUri = Uri.parse("android.resource://$packageName/" + R.drawable.blank_profile)
        }
        val profilePicRef = storageRef.child("profile_picture/${userID.toString()}")
        val uploadTask = profilePicRef.putFile(imageUri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            profilePicRef.downloadUrl
        }.addOnCompleteListener { url ->
            if (url.isSuccessful) {
                userImageUri = url.result
                addData(userImageUri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Upload Failed",Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadData(imageUri: String, email: String, name: String, age: Int, gender: String, job: String) {
        val user = hashMapOf(
            "image_url" to imageUri,
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