package com.munoon.counter

import com.munoon.counter.dateLoaders.DateLoader
import com.munoon.counter.rates.RatesService
import com.munoon.counter.user.User
import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.MessageProperties
import com.munoon.counter.utils.RateUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.time.LocalDate
import java.util.*

@Service
class UsersCommentsList(
        private val messageProperties: MessageProperties,
        private val ratesService: RatesService,
        private val userRepository: UserRepository,
        private val dateLoader: DateLoader,
        @Lazy private val counterMessageSender: CounterMessageSender,
        @Lazy private val telegramBot: TelegramBot
) {
    private val log = LoggerFactory.getLogger(UsersCommentsList::class.java)
    private val callbackDataPrefix = "users_comments"
    private val showCommand = "show"
    private val backCommand = "back"

    fun getMarkup(): InlineKeyboardMarkup {
        val message = messageProperties.getProperty("getOtherCommentsButton")
        val button = InlineKeyboardButton(message)
        button.callbackData = "$callbackDataPrefix $showCommand"
        return InlineKeyboardMarkup(
                Collections.singletonList(
                        Collections.singletonList(button)
                )
        );
    }

    fun onCallbackQuery(callbackQuery: CallbackQuery) {
        if (!validateCallbackQuery(callbackQuery)) {
            return
        }

        val command = callbackQuery.data.split(" ")[1];
        if (command == showCommand) {
            log.info("Show other user's comment to user with telegram chat id ${callbackQuery.message.from.id}")
            showUsersComment(callbackQuery)
        } else {
            log.info("Show counter ('Back' button) to user with telegram chat id ${callbackQuery.message.from.id}")
            onBackCommand(callbackQuery)
        }

        val answerCallbackQuery = AnswerCallbackQuery()
        answerCallbackQuery.callbackQueryId = callbackQuery.id
        telegramBot.execute(answerCallbackQuery)
    }

    private fun showUsersComment(callbackQuery: CallbackQuery) {
        if (dateLoader.getRemainsDays(LocalDate.now()) > 0) {
            return
        }

        val editMessageText = EditMessageText()
        editMessageText.messageId = callbackQuery.message.messageId
        editMessageText.setChatId(callbackQuery.message.chatId)
        editMessageText.setParseMode("Markdown")

        val user = userRepository.getByTelegramChatId(callbackQuery.message.chatId.toString())
        val editMessageTextText = getCommentsMessageText(user)
        editMessageText.text =
                if (editMessageTextText.isEmpty()) messageProperties.getProperty("noRateAvailable")
                else editMessageTextText

        telegramBot.execute(editMessageText)

        val editMessageReplyMarkup = EditMessageReplyMarkup()
        editMessageReplyMarkup.messageId = callbackQuery.message.messageId
        editMessageReplyMarkup.setChatId(callbackQuery.message.chatId)

        val editMessageReplyMarkupButton = InlineKeyboardButton()
        editMessageReplyMarkupButton.text = messageProperties.getProperty("backButton")
        editMessageReplyMarkupButton.callbackData = "$callbackDataPrefix $backCommand"

        editMessageReplyMarkup.replyMarkup = InlineKeyboardMarkup(
                Collections.singletonList(
                        Collections.singletonList(editMessageReplyMarkupButton)
                )
        )
        telegramBot.execute(editMessageReplyMarkup)
    }

    private fun onBackCommand(callbackQuery: CallbackQuery) {
        val editMessageText = EditMessageText()
        editMessageText.messageId = callbackQuery.message.messageId
        editMessageText.setChatId(callbackQuery.message.chatId)
        editMessageText.replyMarkup = getMarkup()

        val rate = ratesService.getLastRate(callbackQuery.from.id.toString())
        editMessageText.text = counterMessageSender.getText(
                rate.marks ?: Collections.emptyList(),
                rate.comment ?: ""
        )

        telegramBot.execute(editMessageText)
    }

    private fun validateCallbackQuery(callbackQuery: CallbackQuery): Boolean {
        val data = callbackQuery.data
        if (!data.startsWith(callbackDataPrefix)) {
            return false
        }

        val dataInfo = data.split(" ")
        return dataInfo.size == 2 && (dataInfo[1] == showCommand || dataInfo[1] == backCommand)
    }

    private fun getCommentsMessageText(user: User): String {
        val builder = StringJoiner("\n")

        userRepository.findAll().forEach {
            if (it.id!! == user.id!!) {
                return@forEach
            }

            val rates = ratesService.getOwnRates(it.id)
            builder.add("*${RateUtil.parsePropertyCharset(it.name)}*\n${RateUtil.printOwnRatesList(rates)}")
        }

        return builder.toString()
    }
}