package com.suprajit.otareflash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.suprajit.otareflash.databinding.ItemDeviceListBinding // Replace with your actual package and layout name

class RvAdapter(private val dataList: List<String>) : RecyclerView.Adapter<RvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeviceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemText = dataList[position]
        holder.binding.tvDeviceName.text = itemText // Access text through binding

        // Set a click listener for the entire item
        holder.itemView.setOnClickListener {
            // Handle the click event for the item here
            // You can access itemText as needed
        }
    }

    override fun getItemCount() = dataList.size

    inner class ViewHolder(val binding: ItemDeviceListBinding) : RecyclerView.ViewHolder(binding.root)
}
