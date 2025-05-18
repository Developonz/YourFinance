// AccountSelectionAdapter.kt
package com.example.yourfinance.presentation.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.IconMap
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemBottomSheetAccountBinding

class AccountSelectionAdapter(
    private val onItemClick: (MoneyAccount) -> Unit
) : ListAdapter<MoneyAccount, AccountSelectionAdapter.VH>(Diff()) {

    private var selectedId: Long? = null

    fun setSelectedAccountId(id: Long?) {
        val old = selectedId
        selectedId = id
        old?.let { o ->
            currentList.indexOfFirst { it.id == o }
                .takeIf { it >= 0 }?.let(::notifyItemChanged)
        }
        selectedId?.let { n ->
            currentList.indexOfFirst { it.id == n }
                .takeIf { it >= 0 }?.let(::notifyItemChanged)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemBottomSheetAccountBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, pos: Int) =
        holder.bind(getItem(pos), selectedId)

    inner class VH(val b: ItemBottomSheetAccountBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(acc: MoneyAccount, sel: Long?) {
            b.textAccountNameBottomSheet.text = acc.title
            b.textAccountBalanceBottomSheet.text = "%.2f ₽".format(acc.balance)

            val iconRes = acc.iconResourceId?.let(IconMap::idOf) ?: R.drawable.ic_mobile_wallet
            val bg = acc.colorHex ?: b.root.context.getColor(R.color.default_icon_background)

            b.imageAccountIconBottomSheet.apply {
                setImageResource(iconRes)
                setBackgroundColor(bg)
                setColorFilter(if (ColorUtils.calculateLuminance(bg) > .5)
                    android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }

            b.imageSelectedCheckBottomSheet.isVisible = (acc.id == sel)
            b.root.setOnClickListener { onItemClick(acc) }
        }
    }

    class Diff : DiffUtil.ItemCallback<MoneyAccount>() {
        override fun areItemsTheSame(o: MoneyAccount, n: MoneyAccount) = o.id == n.id
        override fun areContentsTheSame(o: MoneyAccount, n: MoneyAccount) =
            o.title == n.title && o.balance == n.balance &&
                    o.iconResourceId == n.iconResourceId && o.colorHex == n.colorHex
    }
}
