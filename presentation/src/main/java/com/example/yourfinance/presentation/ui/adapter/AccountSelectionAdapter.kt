package com.example.yourfinance.presentation.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.R // Убедитесь, что R импортирован правильно
import com.example.yourfinance.presentation.databinding.ItemBottomSheetAccountBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import java.text.NumberFormat
import java.util.Locale

class AccountSelectionAdapter(
    private val onItemClickListener: (MoneyAccount) -> Unit
) : ListAdapter<MoneyAccount, AccountSelectionAdapter.AccountViewHolder>(AccountDiffCallback()) {

    private var selectedAccountId: Long? = null

    fun setSelectedAccountId(accountId: Long?) {
        val previousSelectedId = selectedAccountId
        selectedAccountId = accountId
        // Оповещаем об изменении только для старого и нового выбранных элементов,
        // чтобы избежать перерисовки всего списка
        previousSelectedId?.let { id ->
            val oldPosition = currentList.indexOfFirst { it.id == id }
            if (oldPosition != -1) notifyItemChanged(oldPosition)
        }
        selectedAccountId?.let { id ->
            val newPosition = currentList.indexOfFirst { it.id == id }
            if (newPosition != -1) notifyItemChanged(newPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemBottomSheetAccountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AccountViewHolder(binding, onItemClickListener)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position), selectedAccountId)
    }

    class AccountViewHolder(
        private val binding: ItemBottomSheetAccountBinding,
        private val onItemClickListener: (MoneyAccount) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val numberFormat = NumberFormat.getNumberInstance(Locale("ru", "RU")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }

        fun bind(account: MoneyAccount, selectedAccountId: Long?) {
            binding.textAccountNameBottomSheet.text = account.title
            binding.textAccountBalanceBottomSheet.text = "${numberFormat.format(account.balance)} ₽" // TODO: Валюту лучше брать из настроек или счета

            // Установка иконки и ее фона
            binding.imageAccountIconBottomSheet.setImageResource(
                account.iconResourceId ?: R.drawable.ic_mobile_wallet // Дефолтная иконка
            )

            account.colorHex?.let { hex ->
                try {
                    val color = Color.parseColor(hex)
                    binding.imageAccountIconBottomSheet.setBackgroundColor(color)
                    val iconTintColorForSrc = if (ColorUtils.calculateLuminance(color) > 0.5) Color.BLACK else Color.WHITE
                    binding.imageAccountIconBottomSheet.imageTintList = ColorStateList.valueOf(iconTintColorForSrc)
                } catch (e: IllegalArgumentException) {
                    // Ошибка парсинга цвета, используем дефолтный
                    binding.imageAccountIconBottomSheet.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.imageAccountIconBottomSheet.setBackgroundColor(Color.BLACK)
                }
            } ?: run {
                // Цвет не задан, используем дефолтный
                binding.imageAccountIconBottomSheet.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                binding.imageAccountIconBottomSheet.imageTintList = ColorStateList.valueOf(Color.BLACK)
            }

            binding.imageSelectedCheckBottomSheet.isVisible = (account.id == selectedAccountId)

            binding.root.setOnClickListener {
                onItemClickListener(account)
            }
        }
    }

    private class AccountDiffCallback : DiffUtil.ItemCallback<MoneyAccount>() {
        override fun areItemsTheSame(oldItem: MoneyAccount, newItem: MoneyAccount): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoneyAccount, newItem: MoneyAccount): Boolean {
            // Сравниваем все поля, которые влияют на отображение
            return oldItem.title == newItem.title &&
                    oldItem.balance == newItem.balance &&
                    oldItem.iconResourceId == newItem.iconResourceId &&
                    oldItem.colorHex == newItem.colorHex
        }
    }
}