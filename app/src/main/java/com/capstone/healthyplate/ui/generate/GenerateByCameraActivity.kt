package com.capstone.healthyplate.ui.generate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.capstone.healthyplate.createTempFile
import com.capstone.healthyplate.R
import com.capstone.healthyplate.databinding.ActivityGenerateByCameraBinding
import java.io.File

class GenerateByCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateByCameraBinding
    private lateinit var currentPhotoPath: String
    private lateinit var imgBitmap: Bitmap
    private var getFile: File? = null

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
        binding = ActivityGenerateByCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        this.title = resources.getString(R.string.app_nameGenerate)
        binding.imgBtnCamera.setOnClickListener { startTakePhoto() }
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createTempFile(application).also{
            val photoURI: Uri = FileProvider.getUriForFile(
                this@GenerateByCameraActivity,
                "com.capstone.healthyplate",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile

            imgBitmap = BitmapFactory.decodeFile(getFile?.path)
            binding.imgPreview.setImageBitmap(imgBitmap)
        }
    }

    private fun buildModel() {

        val inputLabel = application.assets.open("labels.txt").bufferedReader().use { it.readText() }
        var itemList = inputLabel.split("\n")

        var imgResized: Bitmap = Bitmap.createScaledBitmap(imgBitmap, 300, 300, true)

//        val model = Modelke2.newInstance(this)
//
//        // Creates inputs for reference.
//        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 150, 150, 3), DataType.FLOAT32)
//        var tfImage = TensorImage.fromBitmap(imgResized)
//        var byteBuffer = tfImage.buffer
//        inputFeature0.loadBuffer(byteBuffer)
//
//        // Runs model inference and gets result.
//        val outputs = model.process(inputFeature0)
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//        // Releases model resources if no longer used.
//        model.close()
//
//        var result = outputFeature0.floatArray
//
//        var maxResult = getMax(result)
//        binding.txtIdentifyResult.text = itemList[maxResult]

    }

    fun getMax(label:  FloatArray): Int{
        var index = 0
        var min = 0.5f
        for(i in label.indices){
            if(label[i] > min){
                min = label[i]
                index = i
            }
        }
        return index
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}