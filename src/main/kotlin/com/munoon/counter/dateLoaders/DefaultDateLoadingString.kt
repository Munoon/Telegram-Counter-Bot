package com.munoon.counter.dateLoaders

import com.munoon.counter.configuration.DatesSettings
import com.munoon.counter.utils.MessageProperties
import org.springframework.stereotype.Component

@Component
class DefaultDateLoadingString(
        datesSettings: DatesSettings,
        messageProperties: MessageProperties
) : DateLoader(datesSettings, messageProperties) {
    override fun getLoadingString(percent: Double): String {
        val progress = (percent / 2).toInt()
        val result = Array(50) {
            when {
                it < progress -> "="
                it == progress -> ">"
                else -> " "
            }
        }.joinToString("")
        return "[$result]"
    }
}