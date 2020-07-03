package com.munoon.counter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.telegram.telegrambots.ApiContextInitializer

@EnableScheduling
@SpringBootApplication
class TelegramCounterApplication

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    runApplication<TelegramCounterApplication>(*args)
}