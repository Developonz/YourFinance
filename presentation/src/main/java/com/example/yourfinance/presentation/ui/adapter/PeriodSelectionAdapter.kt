package com.example.yourfinance.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.domain.model.Period
import com.example.yourfinance.presentation.databinding.ItemPeriodSelectionBinding

class PeriodSelectionAdapter(
    private var periods: List<Period>,
    private var currentSelectedPeriod: Period,
    private val onPeriodSelected: (Period) -> Unit
) : RecyclerView.Adapter<PeriodSelectionAdapter.PeriodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodViewHolder {
        val binding = ItemPeriodSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PeriodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PeriodViewHolder, position: Int) {
        val period = periods[position]
        holder.bind(period, period == currentSelectedPeriod)
        holder.itemView.setOnClickListener {
            val oldSelectedPosition = periods.indexOf(currentSelectedPeriod)
            currentSelectedPeriod = period
            notifyItemChanged(oldSelectedPosition) // Обновить старый выбранный
            notifyItemChanged(position)          // Обновить новый выбранный
            onPeriodSelected(period)
        }
    }

    override fun getItemCount(): Int = periods.size

    fun updateSelectedPeriod(newSelectedPeriod: Period) {
        val oldSelectedPosition = periods.indexOf(currentSelectedPeriod)
        val newSelectedPosition = periods.indexOf(newSelectedPeriod)
        currentSelectedPeriod = newSelectedPeriod
        if (oldSelectedPosition != -1) notifyItemChanged(oldSelectedPosition)
        if (newSelectedPosition != -1) notifyItemChanged(newSelectedPosition)
    }


    class PeriodViewHolder(private val binding: ItemPeriodSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(period: Period, isSelected: Boolean) {
            binding.periodName.text = period.description
            binding.periodRadioButton.isChecked = isSelected
            // Тут можно добавить иконки, если решите их использовать
            // when(period) { ... binding.periodIcon.setImageResource(...) }
        }
    }
}