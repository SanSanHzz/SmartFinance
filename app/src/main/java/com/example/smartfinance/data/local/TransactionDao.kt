package com.example.smartfinance.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.smartfinance.data.model.AccountEntity
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
abstract class TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    abstract fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    abstract suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    abstract fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>>

    @Insert
    abstract suspend fun insertTransactionOnly(transaction: TransactionEntity)

    @Update
    abstract suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    abstract suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    abstract suspend fun deleteTransactionById(id: Long)

    @Insert
    abstract suspend fun insertAccount(account: AccountEntity)

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    abstract fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    abstract suspend fun getAccountById(id: Long): AccountEntity?

    @Query("UPDATE accounts SET currentBalance = currentBalance + :amount WHERE id = :accountId")
    abstract suspend fun addToBalance(accountId: Long, amount: Double)

    @Query("UPDATE accounts SET currentBalance = currentBalance - :amount WHERE id = :accountId")
    abstract suspend fun subtractFromBalance(accountId: Long, amount: Double)

    @Transaction
    open suspend fun insertIncome(transaction: TransactionEntity, accountId: Long) {
        insertTransactionOnly(transaction)
        addToBalance(accountId, transaction.amount)
    }

    @Transaction
    open suspend fun insertExpense(transaction: TransactionEntity, accountId: Long) {
        insertTransactionOnly(transaction)
        subtractFromBalance(accountId, transaction.amount)
    }

    @Transaction
    open suspend fun executeTransfer(transaction: TransactionEntity, fromAccountId: Long, toAccountId: Long) {
        insertTransactionOnly(transaction)
        subtractFromBalance(fromAccountId, transaction.amount)
        addToBalance(toAccountId, transaction.amount)
    }

    @Query("""
        SELECT name, COUNT(*) as frequency 
        FROM transactions 
        WHERE type = :type 
        GROUP BY name 
        ORDER BY frequency DESC 
        LIMIT 3
    """)
    abstract fun getTop3FrequentNames(type: TransactionType): Flow<List<TopFrequentName>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND timestamp BETWEEN :startOfMonth AND :endOfMonth")
    abstract fun getMonthlyTotal(type: TransactionType, startOfMonth: Long, endOfMonth: Long): Flow<Double>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'Expense' AND timestamp BETWEEN :startOfMonth AND :endOfMonth GROUP BY category")
    abstract fun getMonthlyExpensesByCategory(startOfMonth: Long, endOfMonth: Long): Flow<List<CategoryTotal>>

    @Transaction
    open suspend fun deleteTransactionAndRevertBalance(id: Long) {
        val tx = getTransactionById(id) ?: return
        when (tx.type) {
            TransactionType.Income -> {
                tx.destinationAccountId?.let { subtractFromBalance(it, tx.amount) }
            }
            TransactionType.Expense -> {
                tx.sourceAccountId?.let { addToBalance(it, tx.amount) }
            }
            TransactionType.Transfer -> {
                tx.destinationAccountId?.let { subtractFromBalance(it, tx.amount) }
                tx.sourceAccountId?.let { addToBalance(it, tx.amount) }
            }
        }
        deleteTransactionById(id)
    }

    @Transaction
    open suspend fun updateTransactionAndBalance(oldTx: TransactionEntity, newTx: TransactionEntity) {
        // Revert old balance
        when (oldTx.type) {
            TransactionType.Income -> oldTx.destinationAccountId?.let { subtractFromBalance(it, oldTx.amount) }
            TransactionType.Expense -> oldTx.sourceAccountId?.let { addToBalance(it, oldTx.amount) }
            TransactionType.Transfer -> {
                oldTx.destinationAccountId?.let { subtractFromBalance(it, oldTx.amount) }
                oldTx.sourceAccountId?.let { addToBalance(it, oldTx.amount) }
            }
        }
        // Apply new balance
        when (newTx.type) {
            TransactionType.Income -> newTx.destinationAccountId?.let { addToBalance(it, newTx.amount) }
            TransactionType.Expense -> newTx.sourceAccountId?.let { subtractFromBalance(it, newTx.amount) }
            TransactionType.Transfer -> {
                newTx.sourceAccountId?.let { subtractFromBalance(it, newTx.amount) }
                newTx.destinationAccountId?.let { addToBalance(it, newTx.amount) }
            }
        }
        updateTransaction(newTx)
    }
}
