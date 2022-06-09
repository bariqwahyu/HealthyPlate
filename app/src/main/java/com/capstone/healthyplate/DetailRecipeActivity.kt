package com.capstone.healthyplate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.capstone.healthyplate.databinding.ActivityDetailRecipeBinding
import com.capstone.healthyplate.ui.account.AccountFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailRecipeBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getData()
    }

    private fun getData() {
        val docRef = db.collection("menu").document("AcarSehatMadu")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val foodName = document.getString("food_name")
                    val photo = document.getString("foto")
                    val ingredients = document.getString("bahan")
                    val step = document.getString("langkah")
                    val kalori: Int = document.getDouble("kalori")!!.toInt()
                    val listIngredients: List<String> = ingredients!!.split(";")
                    val listStep: List<String> = step!!.split(";")
                    binding.apply {
                        txtRecipeNameDetail.text = foodName
                        txtIngredients.text = listIngredients.joinToString("\n")
                        txtStepDetail.text = listStep.joinToString("\n")
                        txtKalori.text = kalori.toString()
                    }
                    if(photo != null) {
                        Glide.with(this)
                            .load(photo)
                            .into(binding.imgRecipeDetail)
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

    companion object {
        private const val TAG = "DetailRecipe"
    }
}