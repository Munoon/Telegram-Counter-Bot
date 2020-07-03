package com.munoon.counter

import com.munoon.counter.user.UserRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Service
class ScheduledService(private val repository: UserRepository, private val telegramBot: TelegramBot) {
    @Scheduled(cron = "\${cronExpression}")
    fun sendScheduledMessage() {
        repository.findAll().forEach {
            val message = SendMessage()
            message.chatId = it.telegramSettings.chatId
            message.text = "Message"
            telegramBot.execute(message)
        }
    }
}