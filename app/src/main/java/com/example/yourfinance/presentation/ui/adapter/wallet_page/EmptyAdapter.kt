package com.example.yourfinance.presentation.ui.adapter.wallet_page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.databinding.EmptyPlaceBinding


class EmptyAdapter: RecyclerView.Adapter<EmptyAdapter.EmptyViewHolder>() {
    class EmptyViewHolder(binding: EmptyPlaceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return EmptyViewHolder(EmptyPlaceBinding.inflate(inflater, parent, false))
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: EmptyViewHolder, position: Int) {}
}