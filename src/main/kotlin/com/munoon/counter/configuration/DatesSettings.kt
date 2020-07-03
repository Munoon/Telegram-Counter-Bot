package com.munoon.counter.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.LocalDate

@Configuration
@ConfigurationProperties(prefix = "dates")
class DatesSettings {
    lateinit var start: LocalDate
    lateinit var end: LocalDate
}