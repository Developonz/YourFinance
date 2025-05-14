package com.example.yourfinance.presentation.ui.adapter


import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.databinding.ItemColorSwatchRectangleBinding

class ColorPickerAdapter(
    private val colors: List<String>,
    private val onColorSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder>() {

    inner class ColorViewHolder(val binding: ItemColorSwatchRectangleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(colorHex: String) {
            try {
                val colorInt = Color.parseColor(colorHex)
                val cardView = binding.cardColorSwatch

                // --- УПРОЩЕНИЕ ---
                // Просто устанавливаем цвет фона CardView
                cardView.setCardBackgroundColor(colorInt)
                // Убедимся, что нет кастомного фона, который мог остаться
//                cardView.background = null
                // --- КОНЕЦ УПРОЩЕНИЯ ---

                cardView.setOnClickListener {
                    Log.d("ColorPickerAdapter", "Clicked on color: $colorHex at position $adapterPosition")
                    onColorSelected(colorHex)
                }
            } catch (e: IllegalArgumentException) {
                Log.e("ColorPickerAdapter", "Invalid color hex: $colorHex", e)
                binding.cardColorSwatch.setCardBackgroundColor(Color.GRAY) // Показываем серый для ошибки
                binding.root.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorSwatchRectangleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position])
    }

    override fun getItemCount(): Int = colors.size
}