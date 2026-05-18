package com.example.smartfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val name: String,
    val amount: Double,
    val place: String? = null,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sourceAccountId: Long? = null,
    val destinationAccountId: Long? = null
)
