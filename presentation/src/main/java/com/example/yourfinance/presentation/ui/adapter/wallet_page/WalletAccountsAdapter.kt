package com.example.yourfinance.presentation.ui.adapter.wallet_page

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.databinding.AccountCreateItemBinding
import com.example.yourfinance.presentation.databinding.AccountItemBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.ui.adapter.list_item.AccountListItem
import com.example.yourfinance.domain.StringHelper


class WalletAccountsAdapter(
    private val newAccountClick: () -> Unit,
    private val editAccountClick: (acc: MoneyAccount) -> Unit
) : ListAdapter<AccountListItem, RecyclerView.ViewHolder>(
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
                    oldItem is AccountListItem.Empty && newItem is AccountListItem.Empty -> true
                    oldItem is AccountListItem.NewAccount && newItem is AccountListItem.NewAccount -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: AccountListItem,
                newItem: AccountListItem
            ): Boolean {
                return (oldItem is AccountListItem.Account &&  newItem is AccountListItem.Account && oldItem.account == newItem.account) ||
                        (oldItem is AccountListItem.Empty && newItem is AccountListItem.Empty) ||
                        (oldItem is AccountListItem.NewAccount && newItem is AccountListItem.NewAccount)

            }
        }

        const val ACCOUNT = 0
        const val EMPTY = 1
        const val NEW = 2
    }

    class MoneyAccountViewHolder(private val binding: AccountItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MoneyAccount, editAccountClick: (acc: MoneyAccount) -> Unit) {
            binding.titleAccount.text = item.title
            binding.balanceAccount.text = StringHelper.getMoneyStr(item.balance)

            binding.infoArea.setOnClickListener {
                editAccountClick(item)
            }
        }
    }

    class EmptyViewHolder(private val binding: AccountItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.infoArea.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    class MoneyAccountCreateViewHolder(private val binding: AccountCreateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(newAccountClick: () -> Unit) {
            binding.infoArea.setOnClickListener {
                newAccountClick()
            }
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
            is AccountListItem.Account -> (holder as MoneyAccountViewHolder).bind(item.account, editAccountClick)
            is AccountListItem.Empty -> (holder as EmptyViewHolder).bind()
            is AccountListItem.NewAccount -> (holder as MoneyAccountCreateViewHolder).bind(newAccountClick)
        }
    }
}
