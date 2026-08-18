package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val adminPassword: String = "Asd1234",
    val whatsappHelpline: String = "+923259550448",
    val exchangeWebsiteUrl: String = "https://bpexch.live",
    val announcementTitle: String = "",
    val announcementMessage: String = ""
)
