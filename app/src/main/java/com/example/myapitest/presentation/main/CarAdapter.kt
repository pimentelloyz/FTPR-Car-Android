package com.example.myapitest.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.databinding.ItemCarLayoutBinding
import com.example.myapitest.domain.model.Car
import com.squareup.picasso.Picasso

class CarAdapter(
    private val onItemClick: (Car) -> Unit,
    private val onItemLongClick: (Car) -> Unit
) : ListAdapter<Car, CarAdapter.CarViewHolder>(CarDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CarViewHolder(private val binding: ItemCarLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(car: Car) {
            binding.model.text = car.name
            binding.year.text = car.year
            binding.license.text = car.licence

            if (car.imageUrl.isNotBlank()) {
                Picasso.get()
                    .load(car.imageUrl)
                    .into(binding.image)
            }

            binding.root.setOnClickListener { onItemClick(car) }
            binding.root.setOnLongClickListener {
                onItemLongClick(car)
                true
            }
        }
    }

    private class CarDiffCallback : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Car, newItem: Car) = oldItem == newItem
    }
}
