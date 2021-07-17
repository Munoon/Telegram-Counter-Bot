package com.munoon.counter.messages

import com.munoon.counter.TelegramBot
import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.MessageProperties
import com.munoon.counter.utils.RateUtil
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val messageProperties: MessageProperties
) {
    private val planingStorage = HashMap<String, LocalDateWrapper>() // user id -> planing date
    private val additionalMessageText = messageProperties.getProperty("additionalMessage")

    fun getAdditionalMessage(date: LocalDate = LocalDate.now()): String? {
        val messages = messageRepository.findAllByDate(date)
        if (messages.isEmpty()) return null

        val message = messages.joinToString(separator = "\n\n")
        { "*${RateUtil.parsePropertyCharset(userRepository.getById(it.userId).name)}*: ${it.message}" }
        return "\n$additionalMessageText\n$message"
    }

    fun onCreateMessageCommand(telegramUserId: String) {
        val user = userRepository.getByTelegramChatId(telegramUserId)
        planingStorage[user.id!!] = LocalDateWrapper(null)
    }

    fun checkMessagePlaningAndParse(update: Update, bot: TelegramBot): Boolean {
        val user = userRepository.getByTelegramChatId(update.message.chatId.toString())
        val planingDate = planingStorage[user.id!!]

        when {
            planingDate == null -> return false
            planingDate.localDate == null -> {
                val date = LocalDate.parse(update.message.text, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                planingStorage[user.id] = LocalDateWrapper(date)
                val text = messageProperties.getProperty("createScheduledMessageStep2")
                bot.execute(SendMessage(update.message.chatId, text))
            }
            else -> {
                messageRepository.save(Message(
                    id = 0,
                    userId = user.id,
                    message = update.message.text,
                    date = planingDate.localDate
                ))
                planingStorage.remove(user.id)

                val text = messageProperties.getProperty("createScheduledMessageDone")
                bot.execute(SendMessage(update.message.chatId, text))
            }
        }

        return true
    }

    private data class LocalDateWrapper(val localDate: LocalDate?)
}