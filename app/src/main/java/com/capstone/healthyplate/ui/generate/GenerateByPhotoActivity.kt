package com.capstone.healthyplate.ui.generate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.healthyplate.R
import com.capstone.healthyplate.databinding.ActivityGenerateByPhotoBinding
import com.capstone.healthyplate.Result
import com.capstone.healthyplate.model.GeneratedListAdapter
import com.capstone.healthyplate.model.GeneratedRecipeList
import com.capstone.healthyplate.model.RecipeListAdapter
import com.capstone.healthyplate.reftrofit.ApiConfig
import com.capstone.healthyplate.reftrofit.ApiService
import com.capstone.healthyplate.response.GenerateResponse
import com.capstone.healthyplate.ui.home.HomeViewModel
import com.capstone.healthyplate.uriToFile
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class GenerateByPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateByPhotoBinding
    private lateinit var generateAdapter: GeneratedListAdapter
    private lateinit var recipeList: ArrayList<GeneratedRecipeList>
    private lateinit var db: FirebaseFirestore
    private var getFile: File? = null
    private val generateViewModel by viewModels<GenerateViewModel>()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.no_permission),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateByPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        this.title = resources.getString(R.string.app_nameGenerate)

        binding.apply {
            btnIdentify.isEnabled = false
            imgBtnCamera.setOnClickListener {
                selectImage()
            }
            btnIdentify.setOnClickListener {
                uploadImage()
            }
            txtIdentifyResult.visibility = View.INVISIBLE
            rvRecipeGenerated.visibility = View.INVISIBLE
        }
    }

    private fun selectImage() {
        val selectImage = Intent(Intent.ACTION_GET_CONTENT)
        selectImage.type = "image/*"
        startActivityForResult(selectImage, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val selectedImg: Uri = data?.data as Uri
        binding.imgPreview.setImageURI(selectedImg)
        val myFile = uriToFile(selectedImg, this@GenerateByPhotoActivity)
        getFile = myFile
        binding.btnIdentify.isEnabled = true
    }

    private fun uploadImage() {
        if (getFile != null) {
            showLoading(true)
            val file = getFile as File
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )
            generateViewModel.uploadPhoto(imageMultipart).observe(this) { result ->
                if (result != null) {
                    when(result) {
                        is Result.Loading -> {
                            showLoading(true)
                        }
                        is Result.Success -> {
                            showLoading(false)
                            binding.rvRecipeGenerated.visibility = View.VISIBLE
                            binding.rvRecipeGenerated.visibility = View.VISIBLE
                            val recipeList = result.data.resultList
                            val listSize = recipeList.indices
                            getRecipeData(listSize, recipeList)
                            setRV()
                        }
                        is Result.Error -> {
                            showLoading(false)
                            Toast.makeText(this, "Error: ${result.error}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        } else {
            Toast.makeText(this, resources.getString(R.string.no_image), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRecipeData(listSize: IntRange, resultList: ArrayList<String?>) {
        for (i in listSize) {
            db = FirebaseFirestore.getInstance()
            db.collection("menu").document(resultList[i]!!)
                .get()
                .addOnSuccessListener { document ->
                    recipeList = arrayListOf()
                    recipeList.clear()

                    recipeList.add((GeneratedRecipeList(
                        document.data!!["food_name"] as String,
                        document.data!!["foto"] as String,
                        document.data!!["bahan"] as String,
                        document.data!!["langkah"] as String
                    )))
                }
                .addOnFailureListener {
                    Log.d(TAG, it.message.toString())
                }
        }
    }

    private fun setRV() {
        generateAdapter = GeneratedListAdapter(recipeList)
        binding.rvRecipeGenerated.layoutManager = LinearLayoutManager(this)
        binding.rvRecipeGenerated.setHasFixedSize(true)
        binding.rvRecipeGenerated.adapter = generateAdapter
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
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val TAG = "GenerateActivity"
    }
}