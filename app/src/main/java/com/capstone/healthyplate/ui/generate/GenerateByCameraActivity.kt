package com.capstone.healthyplate.ui.generate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.capstone.healthyplate.R
import com.capstone.healthyplate.createTempFile
import com.capstone.healthyplate.databinding.ActivityGenerateByCameraBinding
import com.google.firebase.ml.custom.*
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.mlkit.vision.common.InputImage
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class GenerateByCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateByCameraBinding
    private lateinit var currentPhotoPath: String
    private lateinit var imgBitmap: Bitmap
    private lateinit var interpreter: Interpreter
    private lateinit var inputImage: InputImage
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
        model()
        binding.btnIdentify.isEnabled = false
        binding.imgBtnCamera.setOnClickListener {
            selectImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.imgPreview.setImageURI(data?.data)
        var uri: Uri? = data?.data
        imgBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        binding.btnIdentify.isEnabled = true
        //inputImage = InputImage.fromFilePath(this, uri!!)
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
            .getModel("Test-Model", DownloadType.LATEST_MODEL,
                conditions)
            .addOnSuccessListener { model: CustomModel? ->
                // Download complete. Depending on your app, you could enable the ML
                // feature, or switch from the local model to the remote model, etc.

                // The CustomModel object contains the local path of the model file,
                // which you can use to instantiate a TensorFlow Lite interpreter.
                val modelFile = model?.file
                if (modelFile != null) {
                    Log.d("Model : ", modelFile.name)
                    interpreter = Interpreter(modelFile)
                    binding.btnIdentify.setOnClickListener { buildModel(interpreter) }
                }
            }
    }

    private fun localModel() {
        val localModel = FirebaseCustomLocalModel.Builder()
            .setAssetFilePath("local_model_3.tflite")
            .build()
        val options = FirebaseModelInterpreterOptions.Builder(localModel).build()
        val interpreter = FirebaseModelInterpreter.getInstance(options)

        binding.btnIdentify.setOnClickListener {
            val inputOutputOptions = FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 224, 224, 3))
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 5))
                .build()
            val bitmap = Bitmap.createScaledBitmap(imgBitmap, 448, 448, true)
            val input = ByteBuffer.allocateDirect(224*224*3*4).order(ByteOrder.nativeOrder())
            for (y in 0 until 224) {
                for (x in 0 until 224) {
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

            val inputs = FirebaseModelInputs.Builder()
                .add(input) // add() as many input arrays as your model requires
                .build()
            interpreter?.run(inputs, inputOutputOptions)
                ?.addOnSuccessListener { result ->
                    val output = result.getOutput<Array<FloatArray>>(0)
                    val probabilities = output[0]
                    val reader = BufferedReader(
                        InputStreamReader(assets.open("local_labels.txt")))
                    for (i in probabilities.indices) {
                        val label = reader.readLine()
                        Log.i("MLKit", String.format("%s: %1.4f", label, probabilities[i]))
                    }
                }
                ?.addOnFailureListener { e ->
                    Log.d("ML Kit", e.message.toString())
                }
        }
    }

    private fun buildModelLocal() {

    }

    private fun buildModel(interpreter: Interpreter) {

        val inputLabel = application.assets.open("labels2.txt").bufferedReader().use { it.readText() }
        var itemList = inputLabel.split("\n")
//
//        var imgResized: Bitmap = Bitmap.createScaledBitmap(imgBitmap, 300, 300, true)

        val bitmap = Bitmap.createScaledBitmap(imgBitmap, 448, 448, true)
        val input = ByteBuffer.allocateDirect(224*224*3*4).order(ByteOrder.nativeOrder())
        for (y in 0 until 224) {
            for (x in 0 until 224) {
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

        val bufferSize = 5 * java.lang.Float.SIZE / java.lang.Byte.SIZE
        val modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder())
        interpreter.run(input, modelOutput)

        modelOutput.rewind()
        val probabilities = modelOutput.asFloatBuffer()
        try {
            val result = getResult(probabilities)
            binding.txtIdentifyResult.text = itemList[result]
        } catch (e: IOException) {
            // File not found?
        }
    }

    private fun getResult(prob: FloatBuffer): Int {
        val reader = BufferedReader(
            InputStreamReader(application.assets.open("labels2.txt"))
        )
        var index = 0
        var min = 0.5f
        for (i in 0..4) {
            val label: String = reader.readLine()
            val probability = prob.get(i)
            val persen = probability * 100
            val percent = persen.toInt()
            println("$label : $probability = $percent%")
            if (probability > min) {
                min = probability
                index = i
            }
        }
        return index
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
        private const val TAG = "MLKit"
    }
}