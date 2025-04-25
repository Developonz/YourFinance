package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.lifecycle.Observer
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.yourfinance.R
import com.example.yourfinance.databinding.FragmentTransactionContainerBinding
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.presentation.ui.adapter.TransactionPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionContainerFragment : Fragment() {

    private var _binding: FragmentTransactionContainerBinding? = null
    private val binding get() = _binding!!

    // Получаем аргументы из Navigation Component с помощью Safe Args
    private val args: TransactionContainerFragmentArgs by navArgs()
    private val transactionId: Long by lazy { args.transactionId } // ID транзакции, -1L для создания
    private val initialTransactionTypeInt: Int by lazy { args.transactionTypeInt } // Тип транзакции как Int, -1 для создания

    // Определяем, находимся ли мы в режиме редактирования
    // Режим редактирования активен, только если передан валидный ID и Тип
    private val isEditing: Boolean by lazy { transactionId != -1L && initialTransactionTypeInt != -1 }


    // Получаем общую ViewModel, привязанную к жизненному циклу этого контейнера
    // (для Hilt @AndroidEntryPoint фрагмента, by viewModels() по умолчанию scoped к Activity/NavGraph,
    // что в данном случае совпадает с необходимым скоупом для общей ViewModel)
    private val viewModel: TransactionManagerViewModel by viewModels()

    // Ссылка на TabLayoutMediator, чтобы его можно было открепить в onDestroyView
    private var tabLayoutMediator: TabLayoutMediator? = null

    // Callback для отслеживания смены страниц ViewPager2
    // Используется ВСЕГДА (теперь свайп разрешен в обоих режимах)
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            // Определяем тип транзакции по позиции вкладки
            val selectedType = when (position) {
                0 -> TransactionType.EXPENSE
                1 -> TransactionType.INCOME
                2 -> TransactionType.REMITTANCE
                // Это запасной вариант, не должен срабатывать при 3 вкладках
                else -> viewModel.currentTransactionType.value ?: TransactionType.EXPENSE
            }
            // Сообщаем ViewModel о смене активной вкладки (типа транзакции)
            // ViewModel обработает этот вызов: обновит currentTransactionType и сбросит специфичное состояние
            // (Теперь всегда сбрасывает при смене типа)
            viewModel.setTransactionType(selectedType)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("Container", "onViewCreated. isEditing: $isEditing, ID: $transactionId, Initial Type Int: $initialTransactionTypeInt")

        // Устанавливаем заголовок Toolbar/ActionBar в зависимости от режима
        setupToolbarTitle()

        // Настраиваем ViewPager и TabLayout
        setupViewPagerAndTabs()

        if (isEditing) {
            // В режиме редактирования, ViewModel уже начала загрузку данных в своем init{} блоке
            // Наблюдаем за типом загруженной транзакции, чтобы установить нужную вкладку UI после загрузки
            // Этот наблюдатель также убедится, что ViewModel установит initialTransactionType после загрузки
            observeLoadedTransactionTypeAndSetupUI()
        } else {
            // В режиме создания, устанавливаем начальную вкладку UI сразу на основе дефолта ViewModel (EXPENSE)
            val initialPosition = when (viewModel.currentTransactionType.value) {
                TransactionType.INCOME -> 1
                TransactionType.REMITTANCE -> 2
                else -> 0 // EXPENSE или null
            }
            Log.d("Container", "CREATE mode. Setting initial ViewPager position: $initialPosition")
            // Устанавливаем текущую страницу ViewPager2. false означает без плавной прокрутки.
            // Проверяем, не находимся ли уже на нужной странице, чтобы избежать лишних переходов.
            if (binding.viewPager.currentItem != initialPosition) {
                binding.viewPager.setCurrentItem(initialPosition, false)
            } else {
                // Если ViewPager2 уже на начальной позиции (0, Расход), но ViewModel по какой-то причине
                // еще не знает, что активный тип EXPENSE (например, после восстановления состояния без ViewModel),
                // явно сообщаем ей, что активный тип - Расход.
                // ViewModel в этом случае просто установит _currentTransactionType = EXPENSE и сбросит activeState.
                if (initialPosition == 0 && viewModel.currentTransactionType.value != TransactionType.EXPENSE) {
                    Log.d("Container", "Already on position 0, ensuring VM type is EXPENSE.")
                    viewModel.setTransactionType(TransactionType.EXPENSE)
                }
            }
        }

        // Наблюдаем за критической ошибкой загрузки из ViewModel.
        // Эта ошибка обрабатывается и показывает Toast в BaseTransactionInputFragment,
        // который затем сам инициирует popBackStack() для возврата назад.
        // В Container Fragment мы можем просто логировать или добавить дополнительную обработку, если нужно,
        // но основная реакция (Toast и навигация) происходит в Base Fragment.
        viewModel.criticalErrorEvent.observe(viewLifecycleOwner, Observer { errorMessage ->
            Log.e("Container", "Observed criticalErrorEvent in Container: $errorMessage. Fragment should be navigating back.")
        })
    }

    // Устанавливает заголовок в ActionBar/Toolbar в зависимости от режима ViewModel
    private fun setupToolbarTitle() {
        val title = if (viewModel.isEditing) "Редактировать транзакцию" else "Добавить транзакцию"
        (activity as? AppCompatActivity)?.supportActionBar?.title = title
    }

    // Наблюдаем за типом транзакции, загруженной в ViewModel (только в режиме редактирования)
    // Это нужно, чтобы установить правильную вкладку после завершения загрузки данных.
    private fun observeLoadedTransactionTypeAndSetupUI() {
        // Этот наблюдатель срабатывает после того, как loadTransaction в VM завершится и установит _loadedTransactionType
        viewModel.loadedTransactionType.observe(viewLifecycleOwner, Observer { loadedType ->
            loadedType?.let {
                Log.d("Container", "Observed loadedTransactionType: $it. Setting ViewPager.")
                // Определяем позицию вкладки для загруженного типа
                val initialPosition = when (it) {
                    TransactionType.EXPENSE -> 0
                    TransactionType.INCOME -> 1
                    TransactionType.REMITTANCE -> 2
                }
                // Устанавливаем текущую страницу ViewPager2 без анимации
                binding.viewPager.setCurrentItem(initialPosition, false)

                // После того как вкладка установлена, нам больше не нужно наблюдать за этим LiveData,
                // чтобы избежать повторного переключения вкладки при поворотах экрана или других событиях.
                // Важно удалить наблюдателя!
                viewModel.loadedTransactionType.removeObservers(viewLifecycleOwner)

                // Сообщаем ViewModel, что текущий активный тип - это загруженный тип.
                // Это важно для синхронизации ViewModel с UI при загрузке, особенно после восстановления состояния.
                // Метод setTransactionType теперь ВСЕГДА сбрасывает activeState при смене типа,
                // но в режиме редактирования initialTransactionType уже установлен, поэтому
                // первый вызов setTransactionType с загруженным типом не приведет к сбросу (т.к. тип не изменится).
                viewModel.setTransactionType(it)

                Log.d("Container", "Initial ViewPager page set for editing.")
            }
            // Если loadedType пришел null и это режим редактирования,
            // это означает ошибку загрузки, которая уже обрабатывается критическим событием ViewModel
            // и приведет к возврату назад из фрагмента.
        })
    }


    // Настраивает ViewPager2 и связывает его с TabLayout
    private fun setupViewPagerAndTabs() {
        val tabLayout = binding.tabLayoutContainer
        // Используем адаптер, который создает разные фрагменты для разных позиций
        val pagerAdapter = TransactionPagerAdapter(this)

        val viewPager = binding.viewPager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 2 // Держит соседние вкладки в памяти

            // !!! ВНИМАНИЕ: ТЕПЕРЬ СВАЙП РАЗРЕШЕН В ОБОИХ РЕЖИМАХ !!!
            isUserInputEnabled = true // <-- ВСЕГДА РАЗРЕШЕН СВАЙП
        }

        // Создаем и прикрепляем TabLayoutMediator для связи TabLayout и ViewPager2
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Устанавливаем текст для каждой вкладки
            tab.text = when (position) {
                0 -> "Расход"
                1 -> "Доход"
                2 -> "Перевод"
                else -> null
            }
        }
        tabLayoutMediator?.attach() // Прикрепляем медиатор (используем safe call)

        // !!! ВНИМАНИЕ: Слушатель смены страниц ViewPager2 (pageChangeCallback) РЕГИСТРИРУЕТСЯ ВСЕГДА !!!
        viewPager.registerOnPageChangeCallback(pageChangeCallback) // <-- ВСЕГДА РЕГИСТРИРУЕТСЯ
        Log.d("Container", "ViewPager page change callback registered.")

        // !!! ВНИМАНИЕ: Клики по вкладкам TabLayout ТЕПЕРЬ ВСЕГДА РАЗРЕШЕНЫ !!!
        // (Они работают автоматически при прикреплении TabLayoutMediator, если ViewPager isUserInputEnabled = true).
        // Метод disableTabLayoutClicks() и disableViewPagerAndTabs() теперь не нужны и могут быть удалены.
    }

    // !!! УДАЛЯЕМ МЕТОДЫ БЛОКИРОВКИ, ТАК КАК СВАЙП И КЛИКИ РАЗРЕШЕНЫ В РЕЖИМЕ РЕДАКТИРОВАНИЯ !!!
    // private fun disableViewPagerAndTabs() { ... }
    // private fun disableTabLayoutClicks() { ... }


    override fun onDestroyView() {
        super.onDestroyView()
        // Логируем режим и ID при уничтожении View контейнера
        Log.i("Container", "onDestroyView. isEditing: ${viewModel.isEditing}, ID: ${viewModel.isEditing}")

        // Снимаем слушатель смены страниц ViewPager2. Теперь он всегда зарегистрирован.
        // Проверяем binding null здесь, т.к. viewPager привязан к binding.
        if (_binding != null) {
            binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
            Log.d("Container", "ViewPager page change callback unregistered.")
        }

        // Открепляем TabLayoutMediator, чтобы избежать утечек памяти
        tabLayoutMediator?.detach()
        tabLayoutMediator = null // Обнуляем ссылку на медиатор

        // Обнуляем binding, чтобы избежать утечек памяти
        _binding = null
    }

    // Наблюдатели LiveData автоматически отвязываются с viewLifecycleOwner Fragment'а.
    // Callback ViewPager2.OnPageChangeCallback нужно отвязывать вручную (как сделано выше).
}