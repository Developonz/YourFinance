package com.example.yourfinance.view

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.databinding.FragmentTransactionsBinding
import com.example.yourfinance.viewmodel.TransactionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.RecyclerView



class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionsViewModel by activityViewModels()


    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (rootView == null) {
            _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
            rootView = binding.root
            setupRecyclerView()
            setupObservers()
        }
        return rootView!!
    }

    private fun setupRecyclerView() {
        binding.transactionsList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsList.adapter = TransactionsListRecycleViewAdapter(emptyList())
        binding.transactionsList.setHasFixedSize(true)
//        binding.transactionsList.itemAnimator = null
    }

    private fun setupObservers() {
        viewModel.transactionsList.observe(viewLifecycleOwner) { list ->
            (binding.transactionsList.adapter as? TransactionsListRecycleViewAdapter)
                ?.updateData(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}