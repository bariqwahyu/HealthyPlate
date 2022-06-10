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

class RecipeListAdapter(private val recipeList : ArrayList<Recipe>) : RecyclerView.Adapter<RecipeListAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ListViewHolder {
        val binding = ItemGeneratedrecipelistBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(recipeList[position])
    }

    override fun getItemCount(): Int = recipeList.size

    class ListViewHolder(private var binding: ItemGeneratedrecipelistBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(recipe: Recipe) {
            Glide.with(itemView.context)
                .load(recipe.foto)
                .into(binding.imgRecipeGenerated)
            binding.apply {
                txtRecipeNameGenerated.text = recipe.food_name
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailRecipeActivity::class.java)
                intent.putExtra(DetailRecipeActivity.EXTRA_RECIPE, recipe)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.imgRecipeGenerated, "image"),
                        Pair(binding.txtRecipeNameGenerated, "recipeName")
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }


//            itemView.setOnClickListener {
//                val intent = Intent(itemView.context, DetailRecipeActivity::class.java)
//                intent.putExtra(DetailRecipeActivity.EXTRA_STORY, recipe)
//                val optionsCompat: ActivityOptionsCompat =
//                    ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        itemView.context as Activity,
//                        Pair(binding.imgStory, "image"),
//                        Pair(binding.tvRvName, "name"),
//                        Pair(binding.tvRvCreate, "create"),
//                        Pair(binding.tvRvDesc, "desc")
//                    )
//                itemView.context.startActivity(intent, optionsCompat.toBundle())
//            }
        }
    }
}