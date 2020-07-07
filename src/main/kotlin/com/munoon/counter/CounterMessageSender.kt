package com.munoon.counter

import com.munoon.counter.configuration.RatesMarkConfiguration
import com.munoon.counter.dateLoaders.DateLoader
import com.munoon.counter.rates.Rate
import com.munoon.counter.rates.RateRepository
import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.MessageProperties
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
import java.nio.charset.StandardCharsets.ISO_8859_1
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate

// TODO add logging
// TODO add list of own marks
// TODO add list of all marks at the end of counting

@Service
class CounterMessageSender(
        private val userRepository: UserRepository,
        private val rateRepository: RateRepository,
        private val telegramBot: TelegramBot,
        private val messageProperties: MessageProperties,
        private val dateLoader: DateLoader,
        private val ratesMarkConfiguration: RatesMarkConfiguration
) {
    private var log = LoggerFactory.getLogger(CounterMessageSender::class.java)
    private val checkedEmoji = ":heavy_check_mark:"
    private val doneEmoji = ":white_check_mark:"

    @Scheduled(cron = "\${cronExpression}")
    fun sendScheduledMessage() {
        log.info("Sending schedule message to ${userRepository.count()} user(s)")
        userRepository.findAll().forEach {
            val message = SendMessage()
            message.chatId = it.chatId

            message.text = getText()
            message.replyMarkup = getReplyMarkup()

            val sentMessage = telegramBot.execute(message)
            rateRepository.save(Rate(null, it.id.toString(), sentMessage.messageId.toString(), null, null, true))
        }
    }

    fun onMessageReceive(message: Message) {
        val user = userRepository.getByTelegramChatId(message.chatId.toString())
        val rate = rateRepository.getRateByDate(user.id!!, LocalDate.now())
                .orElse(null) ?: return

        telegramBot.execute(DeleteMessage(message.chatId, message.messageId))
        telegramBot.execute(DeleteMessage(message.chatId, rate.messageId.toInt()))

        if (rate.marking) addMark(message, rate) else addComment(message, rate)
    }

    private fun addMark(message: Message, rate: Rate) {
        val messageEmojis = EmojiParser.extractEmojis(message.text)

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
            return
        }

        if (shouldWorkWithMark) {
            if (addEmoji) rate.marks!!.add(emoji) else rate.marks!!.remove(emoji)
            updateCounterMessage(rate, message.chatId, true)
        } else {
            val sendMessage = SendMessage()
            sendMessage.setChatId(message.chatId)
            sendMessage.text = getText(rate.marks!!, messageProperties.getProperty("typeMessageText")!!)
            sendMessage.replyMarkup = ReplyKeyboardRemove()
            val sentMessage = telegramBot.execute(sendMessage)

            rate.marking = false
            rate.messageId = sentMessage.messageId.toString()
            rateRepository.save(rate)
        }
    }

    private fun addComment(message: Message, rate: Rate) {
        rate.comment = message.text
        updateCounterMessage(rate, message.chatId, false)
    }

    private fun updateCounterMessage(rate: Rate, chatId: Long, showMarkup: Boolean) {
        val sendMessage = SendMessage()
        sendMessage.setChatId(chatId)
        sendMessage.text = getText(rate.marks!!, rate.comment ?: "")
        if (showMarkup) {
            sendMessage.replyMarkup = getReplyMarkup(rate.marks!!)
        }
        val sentMessage = telegramBot.execute(sendMessage)

        rate.messageId = sentMessage.messageId.toString()
        rateRepository.save(rate)
    }

    private fun getReplyMarkup(selected: List<String> = emptyList()) = ReplyKeyboardMarkup(
        ratesMarkConfiguration.marks.map {
            val row = KeyboardRow()
            val description = String(it.description.toByteArray(ISO_8859_1), UTF_8)

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

    private fun getText(marks: List<String> = emptyList(), additionalMessage: String = ""): String {
        var text = messageProperties.getProperty(
                "counter",
                dateLoader.getRemainsDays(), dateLoader.getLoadingString()
        )!!

        val additionalText = dateLoader.getAdditionalMessage()
        if (additionalText != null) {
            text += "\n$additionalText"
        }

        if (marks.isNotEmpty()) {
            text += "\n\n${EmojiParser.parseToUnicode(marks.joinToString(""))}"
        }

        if (additionalMessage.isNotEmpty()) {
            text += if (marks.isNotEmpty()) " — $additionalMessage" else "\n\n$additionalMessage"
        }

        return text
    }
}