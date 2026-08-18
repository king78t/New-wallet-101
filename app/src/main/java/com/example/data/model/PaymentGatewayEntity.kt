package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_gateways")
data class PaymentGatewayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gatewayName: String, // e.g. "EasyPaisa Till I'D"
    val currency: String = "PKR", // "PKR", "AED", "SAR", "USD"
    val country: String = "Pakistan",
    val accountTitle: String,
    val accountNumber: String,
    val minDepositAmount: Double = 500.0,
    val isEnabled: Boolean = true
)
