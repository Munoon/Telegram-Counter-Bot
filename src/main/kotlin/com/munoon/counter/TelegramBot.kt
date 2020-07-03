package com.munoon.counter

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBot(
            @Value("\${telegram.bot.token}") private val token: String,
            @Value("\${telegram.bot.username}") private val username: String
        ) : TelegramLongPollingBot() {
    private val log = LoggerFactory.getLogger(TelegramBot::class.java)

    override fun onUpdateReceived(update: Update?) {
        if (update?.message != null) {
            log.info("Unknown message from user ${update.message.from.id}: ${update.message.text}")
        }
    }

    override fun getBotToken(): String = token
    override fun getBotUsername(): String = username
}