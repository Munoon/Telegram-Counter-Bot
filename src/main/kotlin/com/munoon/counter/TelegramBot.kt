package com.munoon.counter

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBot(
            @Value("\${telegram.bot.token}") val token: String,
            @Value("\${telegram.bot.username}") val username: String
        ) : TelegramLongPollingCommandBot() {

    override fun processNonCommandUpdate(update: Update?) {
        if (update?.message != null) {
            val message = SendMessage()
            message.setChatId(update.message.chatId)
            message.text = "Неизвестная команда!"
            execute(message)
        }
    }

    override fun getBotToken(): String = token
    override fun getBotUsername(): String = username
}