package com.capstone.healthyplate

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.capstone.healthyplate.databinding.ActivityCriteriaBinding
import com.capstone.healthyplate.ui.generate.GenerateByCameraActivity
import com.capstone.healthyplate.ui.main.MainActivity
import com.capstone.healthyplate.ui.welcome.WelcomeActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class CriteriaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCriteriaBinding
    private val user = Firebase.auth.currentUser
    private val userID = user?.uid
    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriteriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCriteria()
        binding.btnNext.setOnClickListener {
            addData()
        }
    }

    private fun emailVerification() {
        if (user!!.isEmailVerified()) {
            //enabledET()
            Toast.makeText(this, "User is verified...", Toast.LENGTH_SHORT).show()
        } else {
            //disabledET()
            user.sendEmailVerification()
                .addOnSuccessListener {
                    Toast.makeText(this, "Instructions Sent...", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send due to " + e.message, Toast.LENGTH_SHORT).show()
                }
            Toast.makeText(this, "User isn't verified...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCriteria() {
        val usersRef = db.collection("users").document(userID.toString())
        usersRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val imgView = binding.imgProfileCr
                    val email = document.getString("email")
                    val name = document.getString("name")
                    val age = document.getString("age")
                    val gender = document.getString("gender")
                    val job = document.getString("job")
                    if (email != null && name != null && age != null && gender != null && job != null) {
//                        getProfilePic()
//                        if (imgView.drawable == null){
//                            Glide.with(this)
//                                .load(R.drawable.blank_profile)
//                                .into(binding.imgProfileCr)
//                            binding.apply {
//                                etNameCr.setText(name)
//                                etAgeCr.setText(age)
//                                etGenderCr.setText(gender)
//                                etJobCr.setText(job)
//                            }
//                        } else {
//                            startActivity(Intent(this, MainActivity::class.java))
//                        }
                        startActivity(Intent(this, MainActivity::class.java))
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

    private fun getProfilePic() {
        storageRef.child("profile_picture/${userID.toString()}.png").downloadUrl.addOnSuccessListener { url ->
            Glide.with(this)
                .load(url)
                .into(binding.imgProfileCr)
        }.addOnFailureListener {
            Log.d(TAG, "Get ProfPic : " + it.message.toString())
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
                uploadProfilePicture()
                uploadData(email, name, age, gender, job)
            }
        }
    }

    private fun uploadProfilePicture(){
        val profilePicRef = storageRef.child("profile_picture/${userID.toString()}.png")
        val imageView = binding.imgProfileCr
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = profilePicRef.putBytes(data)
        uploadTask.addOnFailureListener {
            Toast.makeText(this, "Upload Failed",Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            Toast.makeText(this, "Image Uploaded",Toast.LENGTH_SHORT).show()
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