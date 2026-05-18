package com.example.smartfinance.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.smartfinance.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET currentBalance = currentBalance + :amount WHERE id = :accountId")
    suspend fun addToBalance(accountId: Long, amount: Double)

    @Query("UPDATE accounts SET currentBalance = currentBalance - :amount WHERE id = :accountId")
    suspend fun subtractFromBalance(accountId: Long, amount: Double)
}
