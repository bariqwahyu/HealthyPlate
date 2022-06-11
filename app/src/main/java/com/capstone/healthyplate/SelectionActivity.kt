package com.capstone.healthyplate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.healthyplate.databinding.ActivitySelectionBinding
import com.capstone.healthyplate.ui.generate.GenerateByPhotoActivity

class SelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCamera.setOnClickListener { generateByCamera() }
    }

    private fun generateByCamera() {
        startActivity(Intent(this, GenerateByPhotoActivity::class.java))
    }
}