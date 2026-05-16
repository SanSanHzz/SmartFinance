package com.example.smartfinance.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfinance.data.local.AppDatabase
import com.example.smartfinance.data.local.CategoryTotal
import com.example.smartfinance.data.model.TransactionEntity
import com.example.smartfinance.data.model.TransactionType
import com.example.smartfinance.data.repository.TransactionRepository
import com.example.smartfinance.util.EmailSender
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
    val isLoading: Boolean = true
)

data class AccountState(
    val name: String = "",
    val email: String = "",
    val linkedToGmail: Boolean = false,
    val monthlyReport: Boolean = false,
    val emailVerified: Boolean = false
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

    private val _accountState = MutableStateFlow(
        AccountState(
            name = prefs.getString("account_name", "") ?: "",
            email = prefs.getString("account_email", "") ?: "",
            linkedToGmail = prefs.getBoolean("account_gmail", false),
            monthlyReport = prefs.getBoolean("account_report", false),
            emailVerified = prefs.getBoolean("account_email_verified", false)
        )
    )
    val accountState: StateFlow<AccountState> = _accountState.asStateFlow()

    private val _verificationCode = MutableStateFlow("")
    private val _verificationSent = MutableStateFlow(false)
    val verificationSent: StateFlow<Boolean> = _verificationSent.asStateFlow()

    private val _verificationMessage = MutableStateFlow("")
    val verificationMessage: StateFlow<String> = _verificationMessage.asStateFlow()

    private val _reportUri = MutableStateFlow<String?>(null)
    val reportUri: StateFlow<String?> = _reportUri.asStateFlow()

    private var monthStart: Long = 0
    private var monthEnd: Long = 0

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())
        observeData()
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
            repository.getAllTransactions()
        ) { (income, expenses), (topIncome, topExpense), categories, transactions ->
            val healthPct = if (income > 0) (expenses / income * 100).toFloat() else 0f
            _dashboardState.value = DashboardState(
                monthlyIncome = income,
                monthlyExpenses = expenses,
                healthPercentage = healthPct,
                topIncomeNames = topIncome.map { it.name },
                topExpenseNames = topExpense.map { it.name },
                categoryBreakdown = categories,
                isLoading = false
            )
            _allTransactions.value = transactions
        }.launchIn(viewModelScope)
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        prefs.edit().putString("language", lang).apply()
    }

    fun saveAccount(name: String, email: String, linkedToGmail: Boolean, monthlyReport: Boolean) {
        val current = _accountState.value
        _accountState.value = current.copy(
            name = name,
            email = email,
            linkedToGmail = linkedToGmail,
            monthlyReport = monthlyReport
        )
        prefs.edit()
            .putString("account_name", name)
            .putString("account_email", email)
            .putBoolean("account_gmail", linkedToGmail)
            .putBoolean("account_report", monthlyReport)
            .apply()
    }

    fun sendVerificationCode() {
        val email = _accountState.value.email
        if (email.isBlank()) {
            _verificationMessage.value = "Enter an email first"
            return
        }
        viewModelScope.launch {
            val code = (100000..999999).random().toString()
            _verificationCode.value = code
            val result = EmailSender.sendVerificationCode(email, code)
            if (result.isSuccess) {
                _verificationMessage.value = "Code sent to $email"
            } else {
                val ctx = getApplication<android.app.Application>()
                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("mailto:$email")
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "SmartFinance - Verification Code")
                    putExtra(android.content.Intent.EXTRA_TEXT, "Your verification code is: $code")
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(intent)
                _verificationMessage.value = "Code sent via email app"
            }
            _verificationSent.value = true
        }
    }

    fun verifyCode(inputCode: String): Boolean {
        val match = inputCode == _verificationCode.value
        if (match) {
            val current = _accountState.value
            _accountState.value = current.copy(emailVerified = true)
            prefs.edit().putBoolean("account_email_verified", true).apply()
            _verificationMessage.value = "Email verified!"
        } else {
            _verificationMessage.value = "Incorrect code"
        }
        return match
    }

    fun logout() {
        _accountState.value = AccountState()
        prefs.edit()
            .putString("account_name", "")
            .putString("account_email", "")
            .putBoolean("account_gmail", false)
            .putBoolean("account_report", false)
            .putBoolean("account_email_verified", false)
            .apply()
    }

    fun generateReport(context: Context) {
        val state = _dashboardState.value
        val calendar = Calendar.getInstance()
        val monthStr = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val yearStr = calendar.get(Calendar.YEAR).toString()
        val period = "$monthStr/$yearStr"

        val categories = state.categoryBreakdown.map { Pair(it.category, it.total) }
        val data = ReportData(
            monthlyIncome = state.monthlyIncome,
            monthlyExpenses = state.monthlyExpenses,
            healthPercentage = state.healthPercentage,
            categories = categories,
            totalExpenses = state.monthlyExpenses,
            period = period
        )
        val uri = PdfReportGenerator.generateReport(context, data)
        _reportUri.value = uri.toString()
    }

    fun clearReportUri() {
        _reportUri.value = null
    }

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>> =
        repository.getTransactionsByType(type)

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        repository.getTransactionById(id)

    fun addTransaction(
        type: TransactionType,
        name: String,
        amount: Double,
        place: String?,
        category: String
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    type = type,
                    name = name,
                    amount = amount,
                    place = place,
                    category = category
                )
            )
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val samples = listOf(
                TransactionEntity(type = TransactionType.Income, name = "Salary", amount = 3200.0, category = "Salary"),
                TransactionEntity(type = TransactionType.Income, name = "Freelance", amount = 500.0, category = "Salary"),
                TransactionEntity(type = TransactionType.Expense, name = "Burger", amount = 12.50, category = "Food"),
                TransactionEntity(type = TransactionType.Expense, name = "Pizza", amount = 18.00, category = "Food"),
                TransactionEntity(type = TransactionType.Expense, name = "Uber", amount = 8.50, category = "Transport"),
                TransactionEntity(type = TransactionType.Expense, name = "Netflix", amount = 15.99, category = "Entertainment"),
                TransactionEntity(type = TransactionType.Expense, name = "Amazon Box", amount = 45.00, category = "Online Shopping"),
                TransactionEntity(type = TransactionType.Expense, name = "Gas", amount = 40.00, category = "Transport"),
                TransactionEntity(type = TransactionType.Expense, name = "Temu", amount = 23.00, category = "Online Shopping"),
                TransactionEntity(type = TransactionType.Expense, name = "Coffee", amount = 4.50, category = "Food"),
            )
            samples.forEach { repository.insertTransaction(it) }
        }
    }
}
