package com.munoon.counter.dateLoaders

import com.munoon.counter.configuration.DatesSettings
import com.munoon.counter.utils.MessageProperties

class PercentLoadingString(
        datesSettings: DatesSettings,
        val messageProperties: MessageProperties
) : DateLoader(datesSettings, messageProperties) {
    override fun getLoadingString(percent: Double) =
            messageProperties.getProperty("percentLoadingString", percent.toInt())!!
}