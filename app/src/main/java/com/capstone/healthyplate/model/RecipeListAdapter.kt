package com.capstone.healthyplate.model

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.healthyplate.DetailRecipeActivity
import com.capstone.healthyplate.databinding.ItemGeneratedrecipelistBinding

class RecipeListAdapter(private val recipeRVList : ArrayList<RecipeRV>) : RecyclerView.Adapter<RecipeListAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
        val binding = ItemGeneratedrecipelistBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(recipeRVList[position])
    }

    override fun getItemCount(): Int = recipeRVList.size

    class ListViewHolder(private var binding: ItemGeneratedrecipelistBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(recipeRV: RecipeRV) {
            Glide.with(itemView.context)
                .load(recipeRV.foto)
                .into(binding.imgRecipeGenerated)
            binding.apply {
                txtRecipeNameGenerated.text = recipeRV.foodName
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailRecipeActivity::class.java)
                intent.putExtra(DetailRecipeActivity.EXTRA_RECIPE, recipeRV)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.imgRecipeGenerated, "image"),
                        Pair(binding.txtRecipeNameGenerated, "recipeName")
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }
}