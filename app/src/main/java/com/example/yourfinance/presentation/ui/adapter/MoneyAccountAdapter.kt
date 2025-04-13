package com.example.yourfinance.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.databinding.AccountItemBinding
import com.example.yourfinance.databinding.ListItemAccountManagerBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.utils.StringHelper




class MoneyAccountAdapter(
    val deleteClick: (acc: MoneyAccount) -> Unit,
    val editClick: (acc: MoneyAccount) -> Unit
) : ListAdapter<MoneyAccount, MoneyAccountAdapter.MoneyAccountViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MoneyAccount>() {
            override fun areItemsTheSame(
                oldItem: MoneyAccount,
                newItem: MoneyAccount
            ): Boolean {
                return when {
                    oldItem.id == newItem.id -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: MoneyAccount,
                newItem: MoneyAccount
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class MoneyAccountViewHolder(private val binding: ListItemAccountManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: MoneyAccount,
            deleteClick: (acc: MoneyAccount) -> Unit,
            editClick: (acc: MoneyAccount) -> Unit)
        {
            binding.accountTitle.text = item.title
            binding.textAccountBalance.text = StringHelper.getMoneyStr(item.balance)

            binding.imageDelete.setOnClickListener {
                deleteClick(item)
            }

            binding.root.setOnClickListener {
                editClick(item)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoneyAccountViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MoneyAccountViewHolder(ListItemAccountManagerBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: MoneyAccountViewHolder, position: Int) {
        holder.bind(getItem(position), deleteClick, editClick)
    }
}
