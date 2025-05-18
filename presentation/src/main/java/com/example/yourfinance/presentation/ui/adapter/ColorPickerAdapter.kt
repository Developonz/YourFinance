package com.example.yourfinance.presentation.ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.databinding.ItemColorSwatchRectangleBinding

class ColorPickerAdapter(
    private val colors: List<Int>,
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder>() {

    inner class ColorViewHolder(
        private val binding: ItemColorSwatchRectangleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(colorInt: Int) {
            // Устанавливаем цвет фона сразу
            binding.cardColorSwatch.setCardBackgroundColor(colorInt)

            // При клике отдаем Int
            binding.cardColorSwatch.setOnClickListener {
                Log.d("ColorPickerAdapter", "Clicked on color: $colorInt at position $adapterPosition")
                onColorSelected(colorInt)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorSwatchRectangleBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position])
    }

    override fun getItemCount(): Int = colors.size
}
