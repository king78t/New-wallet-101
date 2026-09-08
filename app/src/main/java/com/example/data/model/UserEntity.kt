package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val password: String = "",
    val username: String = "",
    val fullName: String = "",
    val phone: String = "",
    val country: String = "Pakistan",
    val currency: String = "PKR",
    val whatsapp: String = "",
    val role: String = "USER",
    val isApproved: Boolean = true,
    val isBlocked: Boolean = false,
    val walletBalance: Double = 0.0,
    val betProUsername: String = "",
    val betProPassword: String = "",
    val betProStatus: String = "ACTIVE ID",
    val masterAgentName: String = "Pakistan Super Master",
    val createdAt: Long = System.currentTimeMillis()
)
