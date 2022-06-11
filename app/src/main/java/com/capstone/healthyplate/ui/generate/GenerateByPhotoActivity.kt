package com.capstone.healthyplate.ui.generate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.healthyplate.R
import com.capstone.healthyplate.databinding.ActivityGenerateByPhotoBinding
import com.capstone.healthyplate.Result
import com.capstone.healthyplate.uriToFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class GenerateByPhotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateByPhotoBinding
    private lateinit var generateViewModel: GenerateViewModel
    private lateinit var currentPhotoPath: String
    private lateinit var imgBitmap: Bitmap
    private var getFile: File? = null
    private var getUri: Uri? = null

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
        binding.btnIdentify.isEnabled = false
        binding.imgBtnCamera.setOnClickListener {
            selectImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val selectedImg: Uri = data?.data as Uri
        binding.imgPreview.setImageURI(selectedImg)
        val myFile = uriToFile(selectedImg, this@GenerateByPhotoActivity)
        getFile = myFile
        binding.btnIdentify.isEnabled = true
    }

    private fun selectImage() {
        val selectImage = Intent(Intent.ACTION_GET_CONTENT)
        selectImage.type = "image/*"
        startActivityForResult(selectImage, 100)
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
                            Toast.makeText(this, result.data.message, Toast.LENGTH_LONG).show()
                            finish()
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
        private const val TAG = "MLKit"
    }
}