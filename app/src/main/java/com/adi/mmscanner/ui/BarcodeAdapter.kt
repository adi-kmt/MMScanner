package com.adi.mmscanner.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adi.mmscanner.Barcodedata
import com.adi.mmscanner.databinding.BarcodeRvItemBinding

class BarcodeAdapter(val BarcodeList:Array<Barcodedata>): RecyclerView.Adapter<BarcodeAdapter.ItemViewHolder>() {

    class ItemViewHolder(private val binding: BarcodeRvItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(data: Barcodedata){
            binding.apply {
                mainItemTV.text = data.main
                dateItemTV.text = data.date.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = BarcodeRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = this.BarcodeList[position]

        currentItem.let {
            holder.bind(currentItem)
        }
    }

    override fun getItemCount(): Int = BarcodeList.size
}