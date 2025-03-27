package com.example.yourfinance.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
    private lateinit var recyclerView: RecyclerView
    private val adapter = TransactionsListRecycleViewAdapter(emptyList())


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        recyclerView = binding.transactionsList
        recyclerView.layoutManager = LinearLayoutManager(binding.root.context)

        recyclerView.adapter = adapter

        val viewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        viewModel.transactionsList.observe(viewLifecycleOwner) { list ->
            CoroutineScope(Dispatchers.Main).launch {
                adapter.updateData(list)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}