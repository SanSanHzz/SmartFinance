package com.example.smartfinance.data.repository

import com.example.smartfinance.data.local.CategoryTotal
import com.example.smartfinance.data.local.TopFrequentName
import com.example.smartfinance.data.local.TransactionDao
import com.example.smartfinance.data.model.TransactionEntity
import com.example.smartfinance.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun getTransactionById(id: Long): TransactionEntity? = dao.getTransactionById(id)

    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>> = dao.getTransactionsByType(type)

    suspend fun insertTransaction(transaction: TransactionEntity) = dao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) = dao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) = dao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) = dao.deleteTransactionById(id)

    fun getTop3FrequentNames(type: TransactionType): Flow<List<TopFrequentName>> =
        dao.getTop3FrequentNames(type)

    fun getMonthlyTotal(type: TransactionType, startOfMonth: Long, endOfMonth: Long): Flow<Double> =
        dao.getMonthlyTotal(type, startOfMonth, endOfMonth)

    fun getMonthlyExpensesByCategory(startOfMonth: Long, endOfMonth: Long): Flow<List<CategoryTotal>> =
        dao.getMonthlyExpensesByCategory(startOfMonth, endOfMonth)
}
