package com.munoon.counter

import com.munoon.counter.dateLoaders.DateLoader
import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.MessageProperties
import org.slf4j.LoggerFactory
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
    private var log = LoggerFactory.getLogger(CounterMessageSender::class.java)

    @Scheduled(cron = "\${cronExpression}")
    fun sendScheduledMessage() {
        log.info("Sending schedule message to ${repository.count()} user(s)")
        repository.findAll().forEach {
            val message = SendMessage()
            message.chatId = it.telegramSettings.chatId

            var text = messageProperties.getProperty(
                    "counter",
                    dateLoader.getRemainsDays(), dateLoader.getLoadingString()
            )

            val additionalText = dateLoader.getAdditionalMessage()
            if (additionalText != null) {
                text += "\n$additionalText"
            }

            message.text = text
            telegramBot.execute(message)
        }
    }
}