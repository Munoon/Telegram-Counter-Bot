package com.munoon.counter

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBot(
            @Value("\${telegram.bot.token}") private val token: String,
            @Value("\${telegram.bot.username}") private val username: String,
            @Lazy private val counterMessageSender: CounterMessageSender
        ) : TelegramLongPollingBot() {
    private val log = LoggerFactory.getLogger(TelegramBot::class.java)

    override fun onUpdateReceived(update: Update?) {
        if (update?.message != null) {
            counterMessageSender.onMessageReceive(update.message)
        }
    }

    override fun getBotToken(): String = token
    override fun getBotUsername(): String = username
}