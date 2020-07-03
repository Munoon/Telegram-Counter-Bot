package com.munoon.counter

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
@ConfigurationPropertiesBinding
class LocalDateConverter: Converter<String, LocalDate> {
    override fun convert(value: String): LocalDate?
            = LocalDate.parse(value, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
}