package com.munoon.counter

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBot(
            @Value("\${telegram.bot.token}") private val token: String,
            @Value("\${telegram.bot.username}") private val username: String
        ) : TelegramLongPollingBot() {
    override fun onUpdateReceived(update: Update?) {}
    override fun getBotToken(): String = token
    override fun getBotUsername(): String = username
}