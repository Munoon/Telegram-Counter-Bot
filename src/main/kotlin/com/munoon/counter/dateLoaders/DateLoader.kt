package com.munoon.counter.dateLoaders

import com.munoon.counter.configuration.DatesSettings
import com.munoon.counter.utils.MessageProperties
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
abstract class DateLoader(
        protected val datesSettings: DatesSettings,
        private val messageProperties: MessageProperties
) {
    val additionalMessageText = messageProperties.getProperty("additionalMessage")

    fun getRemainsDays(): Double = datesSettings.end.compareTo(LocalDate.now()).toDouble()

    fun getAdditionalMessage(): String? {
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val message = messageProperties.getProperty("counter-message-$date")
                ?: messageProperties.getProperty("counter-message-${getRemainsDays().toInt()}")
                ?: return null
        return "\n$additionalMessageText\n$message"
    }

    abstract fun getLoadingString(): String
}