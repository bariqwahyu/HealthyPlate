package com.capstone.healthyplate.ui.generate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.capstone.healthyplate.createTempFile
import com.capstone.healthyplate.R
import com.capstone.healthyplate.databinding.ActivityGenerateByCameraBinding
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GenerateByCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateByCameraBinding
    private lateinit var currentPhotoPath: String
    private lateinit var imgBitmap: Bitmap
    private lateinit var interpreter: Interpreter
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

        model()

        this.title = resources.getString(R.string.app_nameGenerate)
        binding.imgBtnCamera.setOnClickListener { selectImage() }
        binding.btnIdentify.setOnClickListener { buildModel() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.imgPreview.setImageURI(data?.data)
        var uri: Uri? = data?.data
        imgBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    private fun selectImage() {
        val selectImage = Intent(Intent.ACTION_GET_CONTENT)
        selectImage.type = "image/*"
        startActivityForResult(selectImage, 100)
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

    private fun model() {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()
        FirebaseModelDownloader.getInstance()
            .getModel("Ingredient-Detector", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                conditions)
            .addOnSuccessListener { model: CustomModel? ->
                // Download complete. Depending on your app, you could enable the ML
                // feature, or switch from the local model to the remote model, etc.

                // The CustomModel object contains the local path of the model file,
                // which you can use to instantiate a TensorFlow Lite interpreter.
                val modelFile = model?.file
                if (modelFile != null) {
                    interpreter = Interpreter(modelFile)
                    Log.d("Model : ", modelFile.name)
                }
            }
    }

    private fun buildModel() {

//        val inputLabel = application.assets.open("labels.txt").bufferedReader().use { it.readText() }
//        var itemList = inputLabel.split("\n")
//
//        var imgResized: Bitmap = Bitmap.createScaledBitmap(imgBitmap, 300, 300, true)

        val bitmap = Bitmap.createScaledBitmap(imgBitmap, 300, 300, true)
        val input = ByteBuffer.allocateDirect(150*150*3*4).order(ByteOrder.nativeOrder())
        for (y in 0 until 149) {
            for (x in 0 until 149) {
                val px = bitmap.getPixel(x, y)

                // Get channel values from the pixel value.
                val r = Color.red(px)
                val g = Color.green(px)
                val b = Color.blue(px)

                // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                // For example, some models might require values to be normalized to the range
                // [0.0, 1.0] instead.
                val rf = (r - 127) / 255f
                val gf = (g - 127) / 255f
                val bf = (b - 127) / 255f

                input.putFloat(rf)
                input.putFloat(gf)
                input.putFloat(bf)
            }
        }

        val bufferSize = 22 * java.lang.Float.SIZE / java.lang.Byte.SIZE
        val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
        interpreter.run(input, modelOutput)

        modelOutput.rewind()
        val probabilities = modelOutput.asFloatBuffer()
        try {
            val reader = BufferedReader(
                InputStreamReader(application.assets.open("labels.txt"))
            )
            Log.d("Prob Cap : ",probabilities.capacity().toString())
            for (i in 0..probabilities.capacity()) {
                val label: String = reader.readLine()
                val probability = probabilities.get(i)
                binding.txtIdentifyResult.setText(probability.toString())
                println("$label: $probability")
            }
        } catch (e: IOException) {
            // File not found?
        }
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