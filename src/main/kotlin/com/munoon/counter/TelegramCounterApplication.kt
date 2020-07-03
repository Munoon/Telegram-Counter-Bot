package com.munoon.counter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.ApiContextInitializer

@SpringBootApplication
class TelegramCounterApplication

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    runApplication<TelegramCounterApplication>(*args)
}