package com.example.smartfinance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartfinance.data.model.TransactionEntity
import com.example.smartfinance.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

data class TopFrequentName(
    val name: String,
    val frequency: Int
)

data class CategoryTotal(
    val category: String,
    val total: Double
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("""
        SELECT name, COUNT(*) as frequency 
        FROM transactions 
        WHERE type = :type 
        GROUP BY name 
        ORDER BY frequency DESC 
        LIMIT 3
    """)
    fun getTop3FrequentNames(type: TransactionType): Flow<List<TopFrequentName>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND timestamp BETWEEN :startOfMonth AND :endOfMonth")
    fun getMonthlyTotal(type: TransactionType, startOfMonth: Long, endOfMonth: Long): Flow<Double>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'Expense' AND timestamp BETWEEN :startOfMonth AND :endOfMonth GROUP BY category")
    fun getMonthlyExpensesByCategory(startOfMonth: Long, endOfMonth: Long): Flow<List<CategoryTotal>>
}
