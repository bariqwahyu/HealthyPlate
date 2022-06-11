package com.capstone.healthyplate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.capstone.healthyplate.databinding.ActivityDetailRecipeBinding
import com.capstone.healthyplate.model.RecipeRV
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailRecipeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailRecipeBinding
    private lateinit var foodName: String
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recipeRV = intent.getParcelableExtra<RecipeRV>(EXTRA_RECIPE)
        Glide.with(this)
            .load(recipeRV?.foto)
            .into(binding.imgRecipeDetail)
        binding.apply {
            txtRecipeNameDetail.text = recipeRV?.foodName
            val ingredients = recipeRV?.bahan
            val listIngredients: List<String> = ingredients!!.split(";")
            txtIngredients.text = listIngredients.joinToString("\n")
            val step = recipeRV.langkah
            val listStep: List<String> = step.split(";")
            txtStepDetail.text = listStep.joinToString("\n")

            foodName = recipeRV.foodName.replace(" ","")
        }
        getData(foodName)
    }

    private fun getData(foodName: String) {
        showLoading(true)
        val docRef = db.collection("menu").document(foodName)
        docRef.get()
            .addOnSuccessListener { document ->
                showLoading(false)
                if (document != null) {
                    val kalori = document.get("kalori")
                    val karbohidrat = document.get("karbohidrat")
                    val lemak = document.get("lemak")
                    val protein = document.get("protein")
                    val serat = document.get("serat")

                    binding.apply {
                        val doubleKalori = kalori.toString().toDouble()
                        val intKalori = doubleKalori.toInt()
                        txtKalori.text = "Kalori :\n$intKalori"

                        val doubleKarbohidrat = karbohidrat.toString().toDouble()
                        val intKarbohidrat = doubleKarbohidrat.toInt()
                        txtKarbohidrat.text = "Karbohidrat :\n$intKarbohidrat"

                        val doubleLemak = lemak.toString().toDouble()
                        val intLemak = doubleLemak.toInt()
                        txtLemak.text = "Lemak :\n$intLemak"

                        val doubleProtein = protein.toString().toDouble()
                        val intProtein = doubleProtein.toInt()
                        txtProtein.text = "Protein :\n$intProtein"

                        val doubleSerat = serat.toString().toDouble()
                        val intSerat = doubleSerat.toInt()
                        txtSerat.text = "Serat :\n$intSerat"
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Log.d(TAG, "get failed with ", exception)
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
        const val EXTRA_RECIPE = "extra_recipe"
        private const val TAG = "DetailRecipe"
    }
}