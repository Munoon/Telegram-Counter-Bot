package com.munoon.counter.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "rates")
class RatesMarkConfiguration {
    lateinit var marks: List<RateMark>

    class RateMark {
        lateinit var emoji: String
        lateinit var description: String
    }
}