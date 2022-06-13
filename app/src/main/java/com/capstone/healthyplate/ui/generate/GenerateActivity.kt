package com.capstone.healthyplate.ui.generate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.healthyplate.R
import com.capstone.healthyplate.Result
import com.capstone.healthyplate.databinding.ActivityGenerateBinding
import com.capstone.healthyplate.reftrofit.ApiConfig
import com.capstone.healthyplate.reftrofit.ApiService
import com.capstone.healthyplate.ui.detail.DetailGeneratedActivity
import com.capstone.healthyplate.uriToFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class GenerateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateBinding
    private lateinit var apiService: ApiService
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
        binding = ActivityGenerateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        apiService = ApiConfig.getApiService()

        this.title = resources.getString(R.string.app_nameGenerate)

        binding.apply {
            btnGenerate.isEnabled = false
            imgBtnCamera.setOnClickListener {
                selectImage()
            }
            btnGenerate.setOnClickListener {
                uploadImage()
            }
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
        val myFile = uriToFile(selectedImg, this@GenerateActivity)
        getFile = myFile
        binding.btnGenerate.isEnabled = true
    }

    private fun uploadImage() {
        if (getFile != null) {
            showLoading(true)
            val file = getFile as File
            val requestImageFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "file",
                "telur_asli(1).jpg",
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
                            val resultList = result.data.result as List<String>
                            binding.listResult.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, resultList)
                            binding.listResult.setOnItemClickListener { parent, _, position, _ ->
                                val selectedItem = parent.getItemAtPosition(position) as String
                                val intent = Intent(this, DetailGeneratedActivity::class.java)
                                intent.putExtra(DetailGeneratedActivity.EXTRA_NAME, selectedItem)
                                startActivity(intent)
                            }
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
        private const val TAG = "GenerateActivity"
    }
}