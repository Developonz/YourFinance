package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentTransactionContainerBinding
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.presentation.ui.adapter.TransactionPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionContainerFragment : Fragment() {

    private var _binding: FragmentTransactionContainerBinding? = null
    private val binding get() = _binding!!

    // Args теперь используются ViewModel для начальной инициализации
    // private val args: TransactionContainerFragmentArgs by navArgs()
    // private val transactionId: Long by lazy { args.transactionId }
    // private val initialTransactionTypeInt: Int by lazy { args.transactionTypeInt }
    // private val isEditingModeFromArgs: Boolean by lazy { transactionId != -1L && initialTransactionTypeInt != -1 }

    // ViewModel, общая для контейнера и его дочерних фрагментов
    private val viewModel: TransactionManagerViewModel by viewModels()

    private var tabLayoutMediator: TabLayoutMediator? = null

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val selectedType = when (position) {
                0 -> TransactionType.EXPENSE
                1 -> TransactionType.INCOME
                2 -> TransactionType.REMITTANCE
                else -> viewModel.currentTransactionType.value ?: TransactionType.EXPENSE
            }
            Log.d("Container", "Page selected: $position, Type: $selectedType. Current VM isEditing: ${viewModel.isEditing}")
            viewModel.setTransactionType(selectedType)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionContainerBinding.inflate(inflater, container, false)
        Log.d("Container", "onCreateView. VM isEditing: ${viewModel.isEditing}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("Container", "onViewCreated. VM isEditing: ${viewModel.isEditing}")

        setupToolbarTitle()
        setupViewPagerAndTabs()
        observeViewModel() // Переименовал для ясности, включает все наблюдатели

        // Начальная установка ViewPager на основе состояния ViewModel
        // ViewModel сама инициализируется из NavArgs (через SavedStateHandle)
        if (viewModel.isEditing) {
            // В режиме редактирования, ждем загрузки типа транзакции
            viewModel.loadedTransactionType.observe(viewLifecycleOwner, Observer { loadedType ->
                loadedType?.let {
                    Log.d("Container", "EDIT mode. Observed loadedTransactionType: $it. Setting ViewPager.")
                    val targetPosition = when (it) {
                        TransactionType.EXPENSE -> 0
                        TransactionType.INCOME -> 1
                        TransactionType.REMITTANCE -> 2
                    }
                    if (binding.viewPager.currentItem != targetPosition) {
                        binding.viewPager.setCurrentItem(targetPosition, false) // false для немедленной смены без анимации
                    }
                    // После установки вкладки, можно удалить этот одноразовый наблюдатель, если он больше не нужен
                    // viewModel.loadedTransactionType.removeObservers(viewLifecycleOwner) // Осторожно, если View пересоздается
                }
            })
        } else {
            // Режим создания новой транзакции
            val initialPosition = when (viewModel.currentTransactionType.value) {
                TransactionType.INCOME -> 1
                TransactionType.REMITTANCE -> 2
                else -> 0 // EXPENSE или null (по умолчанию EXPENSE)
            }
            Log.d("Container", "CREATE mode. Setting initial ViewPager position: $initialPosition for type: ${viewModel.currentTransactionType.value}")
            if (binding.viewPager.currentItem != initialPosition) {
                binding.viewPager.setCurrentItem(initialPosition, false)
            }
            // Если ViewPager уже на нужной позиции, но тип в ViewModel мог быть не установлен (маловероятно с новой логикой ViewModel),
            // то onPageSelected при setCurrentItem(..., true) или пользовательский свайп его установит.
            // При setCurrentItem(..., false) onPageSelected может не вызваться, если позиция не изменилась.
            // Убедимся, что ViewModel знает о типе, если позиция 0 и это не EXPENSE
            if (binding.viewPager.currentItem == 0 && viewModel.currentTransactionType.value != TransactionType.EXPENSE){
                viewModel.setTransactionType(TransactionType.EXPENSE) // Сообщаем VM, если она еще не знает
            }
        }
    }

    private fun setupToolbarTitle() {
        val titleRes = if (viewModel.isEditing) R.string.title_edit_transaction else R.string.title_add_transaction
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(titleRes)
    }

    private fun setupViewPagerAndTabs() {
        val pagerAdapter = TransactionPagerAdapter(this) // 'this' - TransactionContainerFragment
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = 2 // Хранить все 3 фрагмента в памяти

        // Разрешить свайп между вкладками всегда
        binding.viewPager.isUserInputEnabled = true

        tabLayoutMediator = TabLayoutMediator(binding.tabLayoutContainer, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_expense)
                1 -> getString(R.string.tab_income)
                2 -> getString(R.string.tab_remittance)
                else -> null
            }
        }
        tabLayoutMediator?.attach()
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
    }

    private fun observeViewModel() {

        viewModel.transactionSavedEvent.observe(viewLifecycleOwner, Observer { saved ->
            if (saved) {
                Log.i("Container", "Transaction saved → closing container")
                Toast.makeText(requireContext(), "Транзакция сохранена", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        })

        viewModel.criticalErrorEvent.observe(viewLifecycleOwner, Observer { errorMessage ->
            Log.e("Container", "Critical error → closing container: $errorMessage")
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
        })


        viewModel.navigateToAccountSettingsEvent.observe(viewLifecycleOwner, Observer {
            Log.d("Container", "Handling navigateToAccountSettingsEvent")
            try {
                findNavController().navigate(R.id.action_transactionContainerFragment_to_accountManagerFragment)
            } catch (e: Exception) {
                Log.e("Container", "Navigation to accountManagerFragment failed", e)
                Toast.makeText(context, R.string.error_nav_account_settings, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.navigateToCategorySettingsEvent.observe(viewLifecycleOwner, Observer {
            try {
                findNavController().navigate(R.id.action_transactionContainerFragment_to_categoriesFragment)
            } catch (e: Exception) {
                Log.e("Container", "Navigation to categoriesFragment failed", e)
                Toast.makeText(context, R.string.error_nav_category_settings, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.showAccountSelectionSheetEvent.observe(viewLifecycleOwner, Observer { request ->
            Log.d("Container", "Handling showAccountSelectionSheetEvent. ForE/I: ${request.isForExpenseIncome}, ForFrom: ${request.isForAccountFrom}")
            // Используем childFragmentManager для BottomSheetDialogFragment, показанного из фрагмента
            if (childFragmentManager.findFragmentByTag(AccountSelectionBottomSheet.TAG) == null) {
                val bottomSheet = AccountSelectionBottomSheet.newInstance(
                    accounts = request.accounts,
                    selectedAccountId = request.selectedId,
                    onAccountSelectedCallback = { selectedAccount ->
                        if (request.isForExpenseIncome) {
                            viewModel.selectPaymentAccount(selectedAccount)
                        } else { // Remittance
                            if (request.isForAccountFrom == true) { // Явно проверяем на true
                                viewModel.selectAccountFrom(selectedAccount)
                            } else { // isForAccountFrom is false (значит, для счета "Куда")
                                viewModel.selectAccountTo(selectedAccount)
                            }
                        }
                    },
                    onSettingsClickedCallback = {
                        viewModel.requestNavigateToAccountSettings() // Запрос навигации через VM
                    }
                )
                bottomSheet.show(childFragmentManager, AccountSelectionBottomSheet.TAG)
            }
        })

        viewModel.criticalErrorEvent.observe(viewLifecycleOwner, Observer { errorMessage ->
            Log.e("Container", "Critical error observed in Container: $errorMessage.")
            // Toast и popBackStack обрабатываются в BaseTransactionInputFragment, но здесь можно добавить специфичную реакцию контейнера, если нужно.
        })

        viewModel.transactionSavedEvent.observe(viewLifecycleOwner, Observer { saved ->
            if (saved) {
                Log.i("Container", "Transaction saved successfully observed in Container.")
                // Toast и popBackStack обрабатываются в BaseTransactionInputFragment.
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Container", "onDestroyView. VM isEditing: ${viewModel.isEditing}")
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        // Очистка адаптера ViewPager, чтобы избежать утечек дочерних фрагментов
        // binding.viewPager.adapter = null // Это важно!
        _binding = null
    }
}