package com.capstone.healthyplate.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capstone.healthyplate.R

class HomeAdapter (private val homeList : ArrayList<Home>) : RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = homeList[position]

        holder.ingredients.text = currentitem.ingredients
    }

    override fun getItemCount(): Int {

        return homeList.size
    }

    class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val ingredients : TextView = itemView.findViewById(R.id.txt_object)
    }
}