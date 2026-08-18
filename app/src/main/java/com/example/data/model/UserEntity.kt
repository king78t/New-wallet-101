package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val password: String,
    val fullName: String,
    val country: String = "Pakistan",
    val currency: String = "PKR",
    val whatsapp: String = "",
    val role: String = "USER", // "USER" or "ADMIN"
    val isApproved: Boolean = true,
    val isBlocked: Boolean = false,
    val walletBalance: Double = 0.0,
    val betProUsername: String = "bpexch_ali101",
    val betProPassword: String = "pass4078",
    val betProStatus: String = "ACTIVE ID", // "ACTIVE ID", "PENDING", "BLOCKED"
    val masterAgentName: String = "Pakistan Super Master"
)
