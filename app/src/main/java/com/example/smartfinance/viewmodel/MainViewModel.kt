package com.example.smartfinance.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfinance.data.local.AppDatabase
import com.example.smartfinance.data.local.CategoryTotal
import com.example.smartfinance.data.model.AccountEntity
import com.example.smartfinance.data.model.TransactionEntity
import com.example.smartfinance.data.model.TransactionType
import com.example.smartfinance.data.repository.TransactionRepository
import com.example.smartfinance.util.PdfReportGenerator
import com.example.smartfinance.util.ReportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardState(
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val healthPercentage: Float = 0f,
    val topIncomeNames: List<String> = emptyList(),
    val topExpenseNames: List<String> = emptyList(),
    val categoryBreakdown: List<CategoryTotal> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val totalNetWorth: Double = 0.0,
    val isLoading: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val prefs = application.getSharedPreferences("smartfinance", Context.MODE_PRIVATE)

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val allTransactions: StateFlow<List<TransactionEntity>> = _allTransactions.asStateFlow()

    private val _currentLanguage = MutableStateFlow(prefs.getString("language", "en") ?: "en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _allAccountsState = MutableStateFlow<List<AccountEntity>>(emptyList())
    val allAccountsState: StateFlow<List<AccountEntity>> = _allAccountsState.asStateFlow()

    private val _reportUri = MutableStateFlow<String?>(null)
    val reportUri: StateFlow<String?> = _reportUri.asStateFlow()

    private var monthStart: Long = 0
    private var monthEnd: Long = 0

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())
        seedDefaultAccounts()
        observeData()
    }

    private fun seedDefaultAccounts() {
        viewModelScope.launch {
            val dao = AppDatabase.getDatabase(getApplication()).transactionDao()
            if (dao.getAccountById(1) == null) {
                dao.insertAccount(AccountEntity(accountName = "Bank", currentBalance = 0.0))
                dao.insertAccount(AccountEntity(accountName = "Cash Wallet", currentBalance = 0.0))
                dao.insertAccount(AccountEntity(accountName = "Savings", currentBalance = 0.0))
            }
        }
    }

    private fun observeData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        monthStart = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        monthEnd = calendar.timeInMillis

        val monthlyIncExp = combine(
            repository.getMonthlyTotal(TransactionType.Income, monthStart, monthEnd),
            repository.getMonthlyTotal(TransactionType.Expense, monthStart, monthEnd)
        ) { inc, exp -> Pair(inc, exp) }

        val topFreq = combine(
            repository.getTop3FrequentNames(TransactionType.Income),
            repository.getTop3FrequentNames(TransactionType.Expense)
        ) { inc, exp -> Pair(inc, exp) }

        combine(
            monthlyIncExp,
            topFreq,
            repository.getMonthlyExpensesByCategory(monthStart, monthEnd),
            repository.getAllTransactions(),
            repository.getAllAccountsFlow()
        ) { (income, expenses), (topIncome, topExpense), categories, transactions, accounts ->
            val healthPct = if (income > 0) (expenses / income * 100).toFloat() else 0f
            val netWorth = accounts.sumOf { it.currentBalance }
            _allAccountsState.value = accounts
            _dashboardState.value = DashboardState(
                monthlyIncome = income,
                monthlyExpenses = expenses,
                healthPercentage = healthPct,
                topIncomeNames = topIncome.map { it.name },
                topExpenseNames = topExpense.map { it.name },
                categoryBreakdown = categories,
                accounts = accounts,
                totalNetWorth = netWorth,
                isLoading = false
            )
            _allTransactions.value = transactions
        }.launchIn(viewModelScope)
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        prefs.edit().putString("language", lang).apply()
    }

    fun generateReport(context: Context) {
        val state = _dashboardState.value
        val transactions = _allTransactions.value
        val calendar = Calendar.getInstance()
        val monthStr = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val yearStr = calendar.get(Calendar.YEAR).toString()
        val period = "$monthStr/$yearStr"

        val data = ReportData(
            monthlyIncome = state.monthlyIncome,
            monthlyExpenses = state.monthlyExpenses,
            healthPercentage = state.healthPercentage,
            categories = state.categoryBreakdown.map { Pair(it.category, it.total) },
            totalExpenses = state.monthlyExpenses,
            period = period,
            transactions = transactions
        )
        _reportUri.value = PdfReportGenerator.generateReport(context, data).toString()
    }

    fun clearReportUri() { _reportUri.value = null }

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>> =
        repository.getTransactionsByType(type)

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        repository.getTransactionById(id)

    fun addTransaction(
        type: TransactionType,
        name: String,
        amount: Double,
        place: String?,
        category: String,
        accountId: Long? = null,
        sourceAccountId: Long? = null,
        destinationAccountId: Long? = null
    ) {
        viewModelScope.launch {
            when (type) {
                TransactionType.Income -> {
                    val accId = accountId ?: destinationAccountId ?: 1L
                    repository.insertIncome(
                        TransactionEntity(type = type, name = name, amount = amount,
                            place = place, category = category, destinationAccountId = accId),
                        accId
                    )
                }
                TransactionType.Expense -> {
                    val accId = accountId ?: sourceAccountId ?: 1L
                    repository.insertExpense(
                        TransactionEntity(type = type, name = name, amount = amount,
                            place = place, category = category, sourceAccountId = accId),
                        accId
                    )
                }
                TransactionType.Transfer -> {
                    val from = sourceAccountId ?: 1L
                    val to = destinationAccountId ?: 2L
                    repository.executeTransfer(
                        TransactionEntity(type = type, name = name, amount = amount,
                            place = place, category = "Transfer",
                            sourceAccountId = from, destinationAccountId = to),
                        from, to
                    )
                }
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val oldTx = repository.getTransactionById(transaction.id)
            if (oldTx != null) {
                repository.updateTransactionAndBalance(oldTx, transaction)
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { repository.deleteTransactionAndRevertBalance(id) }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val r = repository
            r.insertIncome(TransactionEntity(type = TransactionType.Income, name = "Salary", amount = 3200.0, category = "Salary", destinationAccountId = 1), 1)
            r.insertIncome(TransactionEntity(type = TransactionType.Income, name = "Freelance", amount = 500.0, category = "Salary", destinationAccountId = 1), 1)
            r.insertExpense(TransactionEntity(type = TransactionType.Expense, name = "Burger", amount = 12.50, category = "Food", sourceAccountId = 1), 1)
            r.insertExpense(TransactionEntity(type = TransactionType.Expense, name = "Pizza", amount = 18.00, category = "Food", sourceAccountId = 1), 1)
            r.insertExpense(TransactionEntity(type = TransactionType.Expense, name = "Uber", amount = 8.50, category = "Transport", sourceAccountId = 1), 1)
            r.insertExpense(TransactionEntity(type = TransactionType.Expense, name = "Netflix", amount = 15.99, category = "Entertainment", sourceAccountId = 1), 1)
            r.insertExpense(TransactionEntity(type = TransactionType.Expense, name = "Amazon Box", amount = 45.00, category = "Online Shopping", sourceAccountId = 1), 1)
            r.insertExpense(TransactionEntity(type = TransactionType.Expense, name = "Gas", amount = 40.00, category = "Transport", sourceAccountId = 1), 1)
            r.insertExpense(TransactionEntity(type = TransactionType.Expense, name = "Temu", amount = 23.00, category = "Online Shopping", sourceAccountId = 1), 1)
            r.insertExpense(TransactionEntity(type = TransactionType.Expense, name = "Coffee", amount = 4.50, category = "Food", sourceAccountId = 1), 1)
        }
    }
}
