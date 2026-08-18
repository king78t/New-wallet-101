package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val type: String, // "DEPOSIT" or "WITHDRAW"
    val amount: Double,
    val currency: String = "PKR",
    val gatewayName: String = "EasyPaisa Till I'D",
    val accountTitle: String = "",
    val accountNumber: String = "",
    val senderName: String = "",
    val transactionRef: String = "",
    val screenshotUri: String? = null,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)
