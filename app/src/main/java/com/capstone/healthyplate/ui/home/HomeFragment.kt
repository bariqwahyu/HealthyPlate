package com.capstone.healthyplate.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.healthyplate.SelectionActivity
import com.capstone.healthyplate.databinding.FragmentHomeBinding
import com.capstone.healthyplate.model.Recipe
import com.capstone.healthyplate.model.RecipeListAdapter
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recipeList: ArrayList<Recipe>
    private lateinit var recipeAdapter: RecipeListAdapter
    private lateinit var db: FirebaseFirestore

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnGenerateHome.setOnClickListener { toSelectionActivity() }

//        binding.rvRecipeHome.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvRecipeHome.setHasFixedSize(true)
//
//        recipeList = arrayListOf()
//
//        recipeAdapter = RecipeListAdapter(recipeList)

//        binding.rvRecipeHome.adapter = recipeAdapter

        fetchData()

//        val txt_regLoginDesc: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            txt_regLoginDesc.text = it
//        }
        return root
    }

    private fun fetchData() {
        db = FirebaseFirestore.getInstance()
        db.collection("menu")
            .get()
            .addOnSuccessListener {
                recipeList = arrayListOf()
                recipeList.clear()

                for (document in it) {
                    recipeList.add((Recipe(
                        document.data.get("food_name") as String,
                        document.data.get("foto") as String,
                        document.data.get("bahan") as String,
                        document.data.get("langkah") as String,
                    )))
                }

                recipeAdapter = RecipeListAdapter(recipeList)
                binding.rvRecipeHome.layoutManager = GridLayoutManager(requireContext(), 2)
                binding.rvRecipeHome.setHasFixedSize(true)
                binding.rvRecipeHome.adapter = recipeAdapter
            }
    }

    private fun toSelectionActivity() {
        val intent = Intent (activity, SelectionActivity::class.java)
        activity?.startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}