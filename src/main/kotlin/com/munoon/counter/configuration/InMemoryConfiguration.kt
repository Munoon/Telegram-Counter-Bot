package com.munoon.counter.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "repository.inmemory")
class InMemoryConfiguration {
    lateinit var users: List<UserConfiguration>

    class UserConfiguration {
        lateinit var telegramChatId: String
        lateinit var telegramUserId: String
    }
}