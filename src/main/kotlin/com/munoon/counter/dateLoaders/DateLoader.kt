package com.munoon.counter.dateLoaders

import com.munoon.counter.DatesSettings
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
abstract class DateLoader(protected val datesSettings: DatesSettings) {
    fun getRemainsDays(): Double = datesSettings.end.compareTo(LocalDate.now()).toDouble()

    abstract fun getLoadingString(): String
}