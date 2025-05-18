package com.example.yourfinance.presentation.ui.adapter.wallet_page

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.IconMap
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemAccountBinding
import com.example.yourfinance.presentation.databinding.ItemAccountCreateBinding
import com.example.yourfinance.domain.StringHelper
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.ui.adapter.list_item.AccountListItem

class WalletAccountsAdapter(
    private val newAccountClick: () -> Unit,
    private val editAccountClick: (acc: MoneyAccount) -> Unit
) : ListAdapter<AccountListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AccountListItem>() {
            override fun areItemsTheSame(
                oldItem: AccountListItem,
                newItem: AccountListItem
            ): Boolean {
                return when {
                    oldItem is AccountListItem.Account && newItem is AccountListItem.Account ->
                        oldItem.account.id == newItem.account.id
                    oldItem is AccountListItem.Empty && newItem is AccountListItem.Empty -> true
                    oldItem is AccountListItem.NewAccount && newItem is AccountListItem.NewAccount -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: AccountListItem,
                newItem: AccountListItem
            ): Boolean {
                return (oldItem is AccountListItem.Account && newItem is AccountListItem.Account
                        && oldItem.account == newItem.account)
                        || (oldItem is AccountListItem.Empty && newItem is AccountListItem.Empty)
                        || (oldItem is AccountListItem.NewAccount && newItem is AccountListItem.NewAccount)
            }
        }

        private const val ACCOUNT = 0
        private const val EMPTY = 1
        private const val NEW = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AccountListItem.Account     -> ACCOUNT
            is AccountListItem.Empty       -> EMPTY
            is AccountListItem.NewAccount  -> NEW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ACCOUNT -> MoneyAccountViewHolder(
                ItemAccountBinding.inflate(inflater, parent, false),
                editAccountClick
            )
            EMPTY -> EmptyViewHolder(
                ItemAccountBinding.inflate(inflater, parent, false)
            )
            NEW -> MoneyAccountCreateViewHolder(
                ItemAccountCreateBinding.inflate(inflater, parent, false),
                newAccountClick
            )
            else -> throw IllegalArgumentException("Invalid viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val listItem = getItem(position)) {
            is AccountListItem.Account ->
                (holder as MoneyAccountViewHolder).bind(listItem.account)
            is AccountListItem.Empty ->
                (holder as EmptyViewHolder).bind()
            is AccountListItem.NewAccount ->
                (holder as MoneyAccountCreateViewHolder).bind()
        }
    }

    class MoneyAccountViewHolder(
        private val binding: ItemAccountBinding,
        private val editAccountClick: (MoneyAccount) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MoneyAccount) {
            // 1) Резолвим строковый ключ в Int-ресурс
            val resId = item.iconResourceId
                ?.let { IconMap.idOf(it) }
                ?: R.drawable.ic_mobile_wallet
            binding.iconAccount.setImageResource(resId)

            // 2) Цвет фона иконки + контрастный tint
            item.colorHex?.let { colorInt ->
                binding.iconAccount.backgroundTintList =
                    ColorStateList.valueOf(colorInt)
                val tintColor = if (ColorUtils.calculateLuminance(colorInt) > 0.5f)
                    Color.BLACK else Color.WHITE
                binding.iconAccount.imageTintList =
                    ColorStateList.valueOf(tintColor)
            } ?: run {
                // дефолтный фон + чёрный tint
                binding.iconAccount.backgroundTintList =
                    ColorStateList.valueOf(
                        binding.root.context.getColor(R.color.default_icon_background)
                    )
                binding.iconAccount.imageTintList =
                    ColorStateList.valueOf(Color.BLACK)
            }

            // 3) Текстовые поля
            binding.titleAccount.text = item.title
            binding.balanceAccount.text = StringHelper.getMoneyStr(item.balance)

            // 4) Клик по карточке
            binding.infoArea.setOnClickListener {
                editAccountClick(item)
            }
        }
    }

    class EmptyViewHolder(
        private val binding: ItemAccountBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.infoArea.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    class MoneyAccountCreateViewHolder(
        private val binding: ItemAccountCreateBinding,
        private val newAccountClick: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.infoArea.setOnClickListener {
                newAccountClick()
            }
        }
    }
}
