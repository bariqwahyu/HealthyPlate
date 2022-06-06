package com.capstone.healthyplate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.healthyplate.databinding.ActivityDetailRecipeBinding

class DetailRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}