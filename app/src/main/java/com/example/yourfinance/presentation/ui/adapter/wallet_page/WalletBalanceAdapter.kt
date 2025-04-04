package com.example.yourfinance.presentation.ui.adapter.wallet_page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.databinding.BalanceItemBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.utils.StringHelper

class WalletBalanceAdapter(
    var moneyAccounts: List<MoneyAccount> = emptyList()
): RecyclerView.Adapter<WalletBalanceAdapter.WalletBalanceViewHolder>() {


    class WalletBalanceViewHolder(private val binding: BalanceItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(moneyAccounts: List<MoneyAccount>) {
            var sum = 0.0
            moneyAccounts.forEach({
                sum += it.balance
            })
            binding.generalBalance.text = StringHelper.getMoneyStr(sum)
        }
    }

    fun update(accounts: List<MoneyAccount>) {
        this.moneyAccounts = accounts
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletBalanceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return WalletBalanceViewHolder(BalanceItemBinding.inflate(inflater, parent, false))
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: WalletBalanceViewHolder, position: Int) {
        holder.bind(moneyAccounts)
    }
}