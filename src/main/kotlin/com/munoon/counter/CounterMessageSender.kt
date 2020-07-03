package com.munoon.counter

import com.munoon.counter.dateLoaders.DateLoader
import com.munoon.counter.user.UserRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Service
class CounterMessageSender(
        private val repository: UserRepository,
        private val telegramBot: TelegramBot,
        private val messageProperties: MessageProperties,
        private val dateLoader: DateLoader
) {
    @Scheduled(cron = "\${cronExpression}")
    fun sendScheduledMessage() {
        repository.findAll().forEach {
            val message = SendMessage()
            message.chatId = it.telegramSettings.chatId
            message.text = messageProperties.getProperty(
                    "counter",
                    dateLoader.getRemainsDays(), dateLoader.getLoadingString()
            )
            telegramBot.execute(message)
        }
    }
}