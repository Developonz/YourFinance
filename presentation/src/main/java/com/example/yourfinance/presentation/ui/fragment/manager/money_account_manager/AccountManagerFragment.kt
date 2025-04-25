package com.example.yourfinance.presentation.ui.fragment.manager.money_account_manager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.presentation.databinding.FragmentAccountManagerBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.ui.adapter.MoneyAccountAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountManagerFragment : Fragment() {
    private var _binding: FragmentAccountManagerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoneyAccountManagerViewModel by viewModels()
    private val deleteClick = { acc: MoneyAccount ->
        viewModel.deleteAccount(acc)
    }

    private val editClick = { acc: MoneyAccount ->
        val action = AccountManagerFragmentDirections.actionAccountManagerToAccountCreateManager(acc.id)
        findNavController().navigate(action)
    }
    private val adapter: MoneyAccountAdapter = MoneyAccountAdapter(deleteClick, editClick)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountManagerBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupObservers()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonAddAccount.setOnClickListener {
            val action = AccountManagerFragmentDirections.actionAccountManagerToAccountCreateManager()
            findNavController().navigate(action)
        }
    }



    private fun setupObservers() {
        viewModel.accountsList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAccounts.adapter = adapter
        binding.recyclerViewAccounts.setHasFixedSize(true)
//        binding.recyclerViewAccounts.itemAnimator = null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}