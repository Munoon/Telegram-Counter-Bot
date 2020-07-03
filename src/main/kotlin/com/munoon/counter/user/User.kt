package com.munoon.counter.user

data class User(
        val id: String?,
        val telegramSettings: TelegramSettings
)

data class TelegramSettings(
        val chatId: String,
        val userId: String
)