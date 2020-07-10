package com.munoon.counter

import com.munoon.counter.rates.RatesService
import com.munoon.counter.user.User
import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.MessageProperties
import com.munoon.counter.utils.RateUtil
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
        @Lazy private val counterMessageSender: CounterMessageSender,
        private val ratesService: RatesService,
        private val userRepository: UserRepository,
        @Lazy private val telegramBot: TelegramBot
) {
    private val secretID = UUID.randomUUID().toString();
    private val callbackDataPrefix = "users_comments"
    private val showCommand = "show"
    private val backCommand = "back"

    fun getMarkup(): InlineKeyboardMarkup {
        val message = messageProperties.getProperty("getOtherCommentsButton")
        val button = InlineKeyboardButton(message)
        button.callbackData = "$callbackDataPrefix $showCommand $secretID"
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
            showUsersComment(callbackQuery)
        } else {
            onBackCommand(callbackQuery)
        }

        val answerCallbackQuery = AnswerCallbackQuery()
        answerCallbackQuery.callbackQueryId = callbackQuery.id
        telegramBot.execute(answerCallbackQuery)
    }

    private fun showUsersComment(callbackQuery: CallbackQuery) {
        val commands = callbackQuery.data.split(" ")
        if (commands.size != 3 || commands[2] != secretID) {
            return
        }

        val editMessageText = EditMessageText()
        editMessageText.messageId = callbackQuery.message.messageId
        editMessageText.setChatId(callbackQuery.message.chatId)
        editMessageText.setParseMode("Markdown")

        val user = userRepository.getByTelegramChatId(callbackQuery.message.chatId.toString())
        editMessageText.text = getCommentsMessageText(user)

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

        val rate = ratesService.getRateByTelegramId(callbackQuery.from.id.toString(), LocalDate.now())
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
        return dataInfo.size >= 2 && (dataInfo[1] == showCommand || dataInfo[1] == backCommand)
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