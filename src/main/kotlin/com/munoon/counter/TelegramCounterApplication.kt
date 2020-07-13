package com.munoon.counter

import com.munoon.counter.configuration.DatesSettings
import com.munoon.counter.dateLoaders.DateLoader
import com.munoon.counter.dateLoaders.EmojiLoadingString
import com.munoon.counter.dateLoaders.PercentLoadingString
import com.munoon.counter.dateLoaders.SimpleCharLoadingString
import com.munoon.counter.utils.MessageProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.telegrambots.ApiContextInitializer

@EnableScheduling
@SpringBootApplication
class TelegramCounterApplication {
    @Bean
    fun dateLoader(
            @Value("\${loadingString}") selectedDateLoader: Int,
            datesSettings: DatesSettings,
            messageProperties: MessageProperties,
            environment: Environment
    ): DateLoader = when (selectedDateLoader) {
        1 -> SimpleCharLoadingString(datesSettings, messageProperties)
        2 -> EmojiLoadingString(datesSettings, messageProperties, environment)
        3 -> PercentLoadingString(datesSettings, messageProperties)
        else -> SimpleCharLoadingString(datesSettings, messageProperties)
    }
}

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    runApplication<TelegramCounterApplication>(*args)
}