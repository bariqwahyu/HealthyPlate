package com.capstone.healthyplate.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.capstone.healthyplate.SelectionActivity
import com.capstone.healthyplate.databinding.FragmentHomeBinding
import com.capstone.healthyplate.model.RecipeListAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recipeAdapter: RecipeListAdapter
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnGenerateHome.setOnClickListener { toSelectionActivity() }

        homeViewModel.recipeRV.observe(viewLifecycleOwner) { recipe ->
            recipeAdapter = RecipeListAdapter(recipe)
            binding.rvRecipeHome.layoutManager = GridLayoutManager(requireContext(), 2)
            binding.rvRecipeHome.setHasFixedSize(true)
            binding.rvRecipeHome.adapter = recipeAdapter
        }
        return root
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