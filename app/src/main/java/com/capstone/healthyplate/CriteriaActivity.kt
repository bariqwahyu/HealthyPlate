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
import androidx.core.text.isDigitsOnly
import com.bumptech.glide.Glide
import com.capstone.healthyplate.databinding.ActivityCriteriaBinding
import com.capstone.healthyplate.model.Users
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
    private var imageUriString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriteriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getData()

        binding.imgProfileCr.setOnClickListener {
            selectImage()
        }

        //emailVerification()

        val spinner = binding.spGender
        ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

        binding.btnNext.setOnClickListener {
            if (imageUriString == null) {
                if (imageUri == null) {
                    imageUri = Uri.parse("android.resource://$packageName/" + R.drawable.blank_profile)
                    uploadData()
                } else {
                    uploadData()
                }
            } else {
                uploadData()
            }
        }
    }

    private fun emailVerification() {
        val isVerified = ContextCompat.getDrawable(this, R.drawable.ic_check_white_24dp)
        val notVerified = ContextCompat.getDrawable(this, R.drawable.ic_close_red_24dp)
        if (user!!.isEmailVerified) {
            binding.btnVerification.setCompoundDrawables(null, null, isVerified, null)
            binding.btnVerification.text = "User Verified"
            binding.btnVerification.isEnabled = false
            Toast.makeText(this, "User is verified...", Toast.LENGTH_SHORT).show()
        } else {
            binding.btnVerification.setCompoundDrawables(null, null, notVerified, null)
            binding.btnVerification.text = "Send Email Verification"
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

    private fun getData() {
        val dataUser = intent.getParcelableExtra<Users>(EXTRA_USER)
        val imageUriDB = dataUser?.imageUri
        val nameDB = dataUser?.name
        val ageDB = dataUser?.age
        val jobDB = dataUser?.job
        val genderDB = dataUser?.gender
        val age = stringAge(ageDB)
        val image = imgUri(imageUriDB)
        imageUriString = imageUriDB

        Glide.with(this)
            .load(image)
            .circleCrop()
            .into(binding.imgProfileCr)
        binding.etAgeCr.setText(age)
        when {
            nameDB.isNullOrBlank() -> {
                val name = ""
                setET(name, jobDB, genderDB)
            }
            jobDB.isNullOrBlank() -> {
                val job = ""
                setET(nameDB, job, genderDB)
            }
            else -> {
                setET(nameDB, jobDB, genderDB)
            }
        }
    }

    private fun stringAge(ageDB: String?): String {
        val stringAge =
            if (ageDB == "null" || ageDB.isNullOrBlank()) {
            ""
            } else {
                if (ageDB.isDigitsOnly()) {
                    ageDB
                } else {
                    ""
                }
            }
        return stringAge
    }

    private fun imgUri(imageUriDB: String?): Any {
        val image =
            if (imageUriDB.isNullOrBlank()) {
                Uri.parse("android.resource://$packageName/" + R.drawable.blank_profile)
            } else {
                imageUriDB
            }
        return image
    }

    private fun setET(name: String, job: String?, gender: String?) {
        binding.apply {
            etNameCr.setText(name)
            etJobCr.setText(job)
            when (gender) {
                "Male" -> {
                    spGender.setSelection(0)
                }
                "Female" -> {
                    spGender.setSelection(1)
                }
                else -> {
                    spGender.setSelection(0)
                }
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
            Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .into(binding.imgProfileCr)
        }
    }

    private fun uploadData(){
        if (imageUri == null) {
            addData(imageUriString!!)
        } else {
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
                uploadDataToDB(imageUri, email, name, age.toInt(), gender, job)
            }
        }
    }

    private fun uploadDataToDB(imageUri: String, email: String, name: String, age: Int, gender: String, job: String) {
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
        const val EXTRA_USER = "extra_user"
    }
}