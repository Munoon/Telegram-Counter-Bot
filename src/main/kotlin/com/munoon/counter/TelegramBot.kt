package com.munoon.counter

import com.munoon.counter.messages.MessageService
import com.munoon.counter.rates.RatesService
import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.MessageProperties
import com.munoon.counter.utils.RateUtil
import com.munoon.counter.utils.TelegramUtils
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
    private val messageProperties: MessageProperties,
    private val usersCommentsList: UsersCommentsList,
    private val messageService: MessageService
) : TelegramLongPollingBot() {
    private val log = LoggerFactory.getLogger(TelegramBot::class.java)

    override fun onUpdateReceived(update: Update?) {
        if (update?.message != null) {
            when (update.message.text) {
                "/marks" -> {
                    val user = userRepository.getByTelegramChatId(update.message.chatId.toString())
                    var text = ratesService.getOwnRates(user.id!!).let(RateUtil::printOwnRatesList)
                    if (text.isBlank()) text = messageProperties.getProperty("noMarksMessage")!!

                    val textParts = TelegramUtils.splitMessageText(text)
                    textParts.forEach { execute(SendMessage(user.chatId, it)) }

                    log.info("Send own marks to user ${user.id}")
                }
                "/message" -> {
                    messageService.onCreateMessageCommand(update.message.chatId.toString())
                    val text = messageProperties.getProperty("createScheduledMessage")!!
                        .let { TelegramUtils.escapeSpecialCharacters(it) }
                    execute(SendMessage(update.message.chatId, text).enableMarkdown(true))
                }
                else -> {
                    val parsed = messageService.checkMessagePlaningAndParse(update, this)
                    if (!parsed) {
                        counterMessageSender.onMessageReceive(update.message)
                    }
                }
            }
        } else if (update?.callbackQuery != null) {
            usersCommentsList.onCallbackQuery(update.callbackQuery)
        }
    }

    override fun getBotToken(): String = token
    override fun getBotUsername(): String = username
}