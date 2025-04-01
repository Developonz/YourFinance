package com.example.yourfinance.presentation.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
//import androidx.lifecycle.ViewModelProvider
import com.example.yourfinance.databinding.FragmentStatisticBinding
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel


class StatisticFragment : Fragment() {

    private var _binding: FragmentStatisticBinding? = null

    private val viewModel: TransactionsViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}