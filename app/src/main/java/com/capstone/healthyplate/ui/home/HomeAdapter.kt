package com.capstone.healthyplate.ui.home

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeAdapter (private val homeList : ArrayList<Home>) : RecyclerView.Adapter<HomeAdapter.MyViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {

        return homeList.size
    }

    class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val ingredients : TextView = itemView.findViewById(R.id.)
    }
}