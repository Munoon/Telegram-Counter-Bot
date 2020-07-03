package com.munoon.counter.dateLoaders

import com.munoon.counter.configuration.DatesSettings
import com.munoon.counter.utils.MessageProperties
import org.springframework.stereotype.Component

@Component
class DefaultDateLoadingString(
        datesSettings: DatesSettings,
        messageProperties: MessageProperties
) : DateLoader(datesSettings, messageProperties) {
    override fun getLoadingString(): String {
        val compareToday = getRemainsDays()
        val compareTotal = datesSettings.end.compareTo(datesSettings.start).toDouble()
        val progress = (100 - ((compareToday / compareTotal) * 100)).toInt() / 2

        return "[" + Array(50) {
            when {
                it < progress -> "="
                it == progress -> ">"
                else -> " "
            }
        }.joinToString("") + "]"
    }
}