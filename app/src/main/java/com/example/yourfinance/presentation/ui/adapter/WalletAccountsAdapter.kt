package com.example.yourfinance.presentation.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.R
import com.example.yourfinance.databinding.AccountCreateItemBinding
import com.example.yourfinance.databinding.AccountItemBinding
import com.example.yourfinance.databinding.SectionsHeaderBinding
import com.example.yourfinance.domain.model.MoneyAccountsListItem
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.utils.StringHelper


const val ACCOUNT = 0
const val EMPTY = 1
const val NEW = 2

class WalletAccountsAdapter : ListAdapter<AccountListItem, RecyclerView.ViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AccountListItem>() {
            override fun areItemsTheSame(
                oldItem: AccountListItem,
                newItem: AccountListItem
            ): Boolean {
                return when {
                    oldItem is AccountListItem.Account &&  newItem is AccountListItem.Account ->
                            oldItem.account.id == newItem.account.id
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: AccountListItem,
                newItem: AccountListItem
            ): Boolean {
                return oldItem is AccountListItem.Account &&  newItem is AccountListItem.Account && oldItem.account == newItem.account
            }
        }
    }

    class MoneyAccountViewHolder(private val binding: AccountItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MoneyAccount) {
            binding.titleAccount.text = item.title
            binding.balanceAccount.text = StringHelper.getMoneyStr(item.balance)
        }
    }

    class EmptyViewHolder(private val binding: AccountItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.infoArea.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    class MoneyAccountCreateViewHolder(private val binding: AccountCreateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(val item = getItem(position)) {
            is AccountListItem.Account -> ACCOUNT
            is AccountListItem.Empty -> EMPTY
            is AccountListItem.NewAccount -> NEW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            ACCOUNT -> MoneyAccountViewHolder(AccountItemBinding.inflate(inflater, parent, false))
            EMPTY -> EmptyViewHolder(AccountItemBinding.inflate(inflater, parent, false))
            NEW -> MoneyAccountCreateViewHolder(AccountCreateItemBinding.inflate(inflater, parent, false))
            else -> EmptyViewHolder(AccountItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = getItem(position)) {
            is AccountListItem.Account -> (holder as MoneyAccountViewHolder).bind(item.account)
            is AccountListItem.Empty -> (holder as EmptyViewHolder).bind()
            is AccountListItem.NewAccount -> (holder as MoneyAccountCreateViewHolder).bind()
        }
    }
}
