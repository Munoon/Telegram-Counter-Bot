package com.munoon.counter

import com.munoon.counter.configuration.RatesMarkConfiguration
import com.munoon.counter.dateLoaders.DateLoader
import com.munoon.counter.messages.MessageService
import com.munoon.counter.rates.Rate
import com.munoon.counter.rates.RatesService
import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.MessageProperties
import com.munoon.counter.utils.RateUtil
import com.munoon.counter.utils.TelegramUtils
import com.vdurmont.emoji.EmojiParser
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.time.LocalDate

@Service
class CounterMessageSender(
        private val userRepository: UserRepository,
        private val ratesService: RatesService,
        private val telegramBot: TelegramBot,
        private val messageProperties: MessageProperties,
        private val dateLoader: DateLoader,
        private val ratesMarkConfiguration: RatesMarkConfiguration,
        private val usersCommentsList: UsersCommentsList,
        private val messageService: MessageService
) {
    private val log = LoggerFactory.getLogger(CounterMessageSender::class.java)
    private val checkedEmoji = ":heavy_check_mark:"
    private val doneEmoji = ":white_check_mark:"

    @Scheduled(cron = "\${cronExpression}")
    fun sendScheduledMessage() {
        log.info("Sending schedule message to ${userRepository.count()} user(s)")
        val showOtherUsersComment = dateLoader.getRemainsDays(LocalDate.now()) <= 0;
        userRepository.findAll().forEach {
            val message = SendMessage().enableMarkdown(true)
            message.chatId = it.chatId

            message.text = TelegramUtils.escapeSpecialCharacters(getText())
            message.replyMarkup = if (showOtherUsersComment) usersCommentsList.getMarkup() else getReplyMarkup()

            val sentMessage = telegramBot.execute(message)
            ratesService.save(Rate(null, it.id.toString(), sentMessage.messageId.toString(), null, null, true))
        }
    }

    fun onMessageReceive(message: Message) {
        val rate = ratesService.getLastRate(message.from.id.toString())

        telegramBot.execute(DeleteMessage(message.chatId, message.messageId))
        telegramBot.execute(DeleteMessage(message.chatId, rate.messageId.toInt()))

        if (rate.marking) addMark(message, rate) else addComment(message, rate)
    }

    private fun addMark(message: Message, rate: Rate) {
        val messageEmojis = EmojiParser.extractEmojis(message.text)
        if (messageEmojis.size == 0) {
            updateCounterMessage(rate, message.chatId, true)
            return
        }

        var emoji = EmojiParser.parseToAliases(messageEmojis[0])
        var addEmoji = true
        var shouldWorkWithMark = true

        if (emoji == checkedEmoji) {
            if (messageEmojis.size == 1) return
            emoji = EmojiParser.parseToAliases(messageEmojis[1])
            addEmoji = false
        } else if (emoji == doneEmoji) {
            shouldWorkWithMark = false
        }

        if (!ratesMarkConfiguration.marks.any { it.emoji == emoji } && emoji != doneEmoji) {
            updateCounterMessage(rate, message.chatId, true)
            return
        }

        if (shouldWorkWithMark) {
            if (addEmoji) {
                log.info("Added '$emoji' mark by user with telegram chat id ${message.from.id}")
                rate.marks!!.add(emoji)
            } else {
                log.info("Removed '$emoji' mark by user with telegram chat id ${message.from.id}")
                rate.marks!!.remove(emoji)
            }
            updateCounterMessage(rate, message.chatId, true)
        } else {
            log.info("Saved marks list and ask for comment user with telegram chat id ${message.from.id}")

            val sendMessage = SendMessage().enableMarkdown(true)
            sendMessage.setChatId(message.chatId)
            sendMessage.text = getText(rate.marks!!, messageProperties.getProperty("typeMessageText")!!)
                .let { TelegramUtils.escapeSpecialCharacters(it) }
            sendMessage.replyMarkup = ReplyKeyboardRemove()
            val sentMessage = telegramBot.execute(sendMessage)

            rate.marking = false
            rate.messageId = sentMessage.messageId.toString()
            ratesService.save(rate)
        }
    }

    private fun addComment(message: Message, rate: Rate) {
        log.info("User with telegram chat id ${message.chatId} add comment: '${message.text}'")
        rate.comment = message.text
        updateCounterMessage(rate, message.chatId, false)
    }

    private fun updateCounterMessage(rate: Rate, chatId: Long, showMarkup: Boolean) {
        val sendMessage = SendMessage().enableMarkdown(true)
        sendMessage.setChatId(chatId)
        sendMessage.text = getText(rate.marks!!, rate.comment ?: "")
            .let { TelegramUtils.escapeSpecialCharacters(it) }

        when {
            showMarkup -> getReplyMarkup(rate.marks!!)
            dateLoader.getRemainsDays(LocalDate.now()) <= 0 -> usersCommentsList.getMarkup()
            else -> null
        }?.let { sendMessage.replyMarkup = it }

        val sentMessage = telegramBot.execute(sendMessage)

        rate.messageId = sentMessage.messageId.toString()
        ratesService.save(rate)
    }

    private fun getReplyMarkup(selected: List<String> = emptyList()) = ReplyKeyboardMarkup(
        ratesMarkConfiguration.marks.map {
            val row = KeyboardRow()
            val description = RateUtil.parsePropertyCharset(it.description)

            var text = messageProperties.getProperty("buttonDescription", it.emoji, description)
            if (selected.contains(it.emoji)) {
                text = EmojiParser.parseToUnicode(checkedEmoji) + text
            }

            row.add(text)
            row
        }.toMutableList().apply {
            val row = KeyboardRow()
            row.add(messageProperties.getProperty("doneRateButton", doneEmoji))
            add(row)
        }
    )

    fun getText(marks: List<String> = emptyList(), additionalMessage: String = ""): String {
        val counterEnd = dateLoader.getRemainsDays(LocalDate.now()) <= 0;
        var text = messageProperties.getProperty(
                if (counterEnd) "counterEnd" else "counter",
                dateLoader.getRemainsDays(), dateLoader.getLoadingString()
        )!!

        val additionalText = messageService.getAdditionalMessage()
        if (additionalText != null) {
            text += "\n$additionalText"
        }

        val comments = RateUtil.getMarksAndComment(marks, additionalMessage).let(EmojiParser::parseToUnicode)
        if (comments.isNotBlank()) {
            text += "\n\n$comments"
        }

        return text
    }
}