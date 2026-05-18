package com.example.smartfinance.data.repository

import com.example.smartfinance.data.local.CategoryTotal
import com.example.smartfinance.data.local.TopFrequentName
import com.example.smartfinance.data.local.TransactionDao
import com.example.smartfinance.data.model.AccountEntity
import com.example.smartfinance.data.model.TransactionEntity
import com.example.smartfinance.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun getTransactionById(id: Long): TransactionEntity? = dao.getTransactionById(id)

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>> = dao.getTransactionsByType(type)

    suspend fun insertTransactionOnly(transaction: TransactionEntity) = dao.insertTransactionOnly(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) = dao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) = dao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) = dao.deleteTransactionById(id)

    suspend fun insertIncome(transaction: TransactionEntity, accountId: Long) =
        dao.insertIncome(transaction, accountId)

    suspend fun insertExpense(transaction: TransactionEntity, accountId: Long) =
        dao.insertExpense(transaction, accountId)

    suspend fun executeTransfer(transaction: TransactionEntity, fromAccountId: Long, toAccountId: Long) =
        dao.executeTransfer(transaction, fromAccountId, toAccountId)

    suspend fun insertAccount(account: AccountEntity) = dao.insertAccount(account)

    fun getAllAccountsFlow(): Flow<List<AccountEntity>> = dao.getAllAccountsFlow()

    suspend fun getAccountById(id: Long): AccountEntity? = dao.getAccountById(id)

    suspend fun addToBalance(accountId: Long, amount: Double) = dao.addToBalance(accountId, amount)

    suspend fun subtractFromBalance(accountId: Long, amount: Double) = dao.subtractFromBalance(accountId, amount)

    fun getTop3FrequentNames(type: TransactionType): Flow<List<TopFrequentName>> =
        dao.getTop3FrequentNames(type)

    fun getMonthlyTotal(type: TransactionType, startOfMonth: Long, endOfMonth: Long): Flow<Double> =
        dao.getMonthlyTotal(type, startOfMonth, endOfMonth)

    fun getMonthlyExpensesByCategory(startOfMonth: Long, endOfMonth: Long): Flow<List<CategoryTotal>> =
        dao.getMonthlyExpensesByCategory(startOfMonth, endOfMonth)
}
