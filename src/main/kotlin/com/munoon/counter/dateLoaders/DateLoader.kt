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
    private val additionalMessageText = messageProperties.getProperty("additionalMessage")

    fun getAdditionalMessage(date: LocalDate = LocalDate.now()): String? {
        val strDate = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        val message = messageProperties.getProperty("counter-message-$strDate")
                ?: messageProperties.getProperty("counter-message-${getRemainsDays(date).toInt()}")
                ?: return null
        return "\n$additionalMessageText\n$message"
    }

    fun getLoadingString(date: LocalDate = LocalDate.now()): String {
        val percent = calculatePercent(date)
        return getLoadingString(percent)
    }

    fun calculatePercent(date: LocalDate): Double {
        val compareToday = getRemainsDays(date)
        val compareTotal = datesSettings.end.compareTo(datesSettings.start).toDouble()
        return 100 - ((compareToday / compareTotal) * 100)
    }

    fun getRemainsDays(fromDate: LocalDate = LocalDate.now()): Double = datesSettings.end.compareTo(fromDate).toDouble()

    abstract fun getLoadingString(percent: Double): String
}