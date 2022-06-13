package com.capstone.healthyplate.ui.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.capstone.healthyplate.databinding.ActivityDetailGeneratedBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailGeneratedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailGeneratedBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailGeneratedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recipe = intent.getStringExtra(EXTRA_NAME)
        val foodName = recipe!!.replace(" ","")
        getData(foodName)
    }

    private fun getData(foodName: String) {
        showLoading(true)
        val docRef = db.collection("menu").document(foodName)
        docRef.get()
            .addOnSuccessListener { document ->
                showLoading(false)
                if (document != null) {
                    val name = document.getString("food_name")
                    val foto = document.getString("foto")
                    val bahan = document.getString("bahan")
                    val langkah = document.getString("langkah")
                    val kalori = document.get("kalori")
                    val karbohidrat = document.get("karbohidrat")
                    val lemak = document.get("lemak")
                    val protein = document.get("protein")
                    val serat = document.get("serat")

                    Glide.with(this)
                        .load(foto)
                        .into(binding.imgRecipeGenerated)
                    binding.apply {
                        txtRecipeNameGenerated.text = name
                        val ingredients = bahan
                        val listIngredients: List<String> = ingredients!!.split(";")
                        txtIngredientsGenerated.text = listIngredients.joinToString("\n")

                        val listStep: List<String> = langkah!!.split(";")
                        txtStepDetailGenerated.text = listStep.joinToString("\n")

                        val doubleKalori = kalori.toString().toDouble()
                        val intKalori = doubleKalori.toInt()
                        txtKaloriGenerated.text = "Kalori :\n$intKalori"

                        val doubleKarbohidrat = karbohidrat.toString().toDouble()
                        val intKarbohidrat = doubleKarbohidrat.toInt()
                        txtKarbohidratGenerated.text = "Karbohidrat :\n$intKarbohidrat"

                        val doubleLemak = lemak.toString().toDouble()
                        val intLemak = doubleLemak.toInt()
                        txtLemakGenerated.text = "Lemak :\n$intLemak"

                        val doubleProtein = protein.toString().toDouble()
                        val intProtein = doubleProtein.toInt()
                        txtProteinGenerated.text = "Protein :\n$intProtein"

                        val doubleSerat = serat.toString().toDouble()
                        val intSerat = doubleSerat.toInt()
                        txtSeratGenerated.text = "Serat :\n$intSerat"
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
        const val EXTRA_NAME = "extra_name"
        private const val TAG = "DetailRecipe"
    }
}