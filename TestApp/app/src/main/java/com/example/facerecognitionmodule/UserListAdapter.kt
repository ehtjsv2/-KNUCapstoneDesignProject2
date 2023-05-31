package com.example.facerecognitionmodule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.facerecognitionmodule.databinding.UserItemBinding


class UserListAdapter(val users: MutableList<User>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    class MyViewHolder(val binding: UserItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(UserItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MyViewHolder).binding
        val current= users.get(position)
        binding.userId.text=current.ID
        binding.cnt1.text=current.americano_cnt.toString()
        binding.cnt2.text=current.caffelatte_cnt.toString()
        binding.cnt3.text=current.cappuccino_cnt.toString()
        binding.cnt4.text=current.coldbrew_cnt.toString()
        binding.cnt5.text=current.caffemocah_cnt.toString()
    }

}