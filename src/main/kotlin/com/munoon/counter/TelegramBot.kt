package com.munoon.counter

import com.munoon.counter.rates.RatesService
import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.MessageProperties
import com.munoon.counter.utils.RateUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class TelegramBot(
            @Value("\${telegram.bot.token}") private val token: String,
            @Value("\${telegram.bot.username}") private val username: String,
            @Lazy private val counterMessageSender: CounterMessageSender,
            private val ratesService: RatesService,
            private val userRepository: UserRepository,
            private val messageProperties: MessageProperties
        ) : TelegramLongPollingBot() {
    private val log = LoggerFactory.getLogger(TelegramBot::class.java)

    override fun onUpdateReceived(update: Update?) {
        if (update?.message != null) {
            if (update.message.text == "/marks") {
                val user = userRepository.getByTelegramChatId(update.message.chatId.toString())
                var text = ratesService.getOwnRates(user.id!!).let(RateUtil::printOwnRatesList)
                if (text.isBlank()) text = messageProperties.getProperty("noMarksMessage")!!
                execute(SendMessage(update.message.chatId, text))
                log.info("Send own marks to user ${user.id}")
            } else {
                counterMessageSender.onMessageReceive(update.message)
            }
        }
    }

    override fun getBotToken(): String = token
    override fun getBotUsername(): String = username
}