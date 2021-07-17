package com.munoon.counter.dateLoaders

import com.munoon.counter.configuration.DatesSettings
import com.munoon.counter.utils.MessageProperties
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Component
abstract class DateLoader(
        protected val datesSettings: DatesSettings,
        private val messageProperties: MessageProperties
) {
    fun getLoadingString(date: LocalDate = LocalDate.now()): String {
        val percent = calculatePercent(date)
        return getLoadingString(percent)
    }

    fun calculatePercent(date: LocalDate): Double {
        val compareToday = getRemainsDays(date)
        val compareTotal = ChronoUnit.DAYS.between(datesSettings.start, datesSettings.end).toDouble()
        return 100 - ((compareToday / compareTotal) * 100)
    }

    fun getRemainsDays(fromDate: LocalDate = LocalDate.now()) = ChronoUnit.DAYS.between(fromDate, datesSettings.end)

    abstract fun getLoadingString(percent: Double): String
}