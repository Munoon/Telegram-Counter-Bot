package com.munoon.counter

import com.vdurmont.emoji.EmojiParser
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.text.MessageFormat
import java.util.*
import javax.annotation.PostConstruct

@Component
class MessageProperties {
    private lateinit var properties: Properties

    @PostConstruct
    fun loadProperties() {
        val resource = ClassPathResource("messages.xml")
        properties = Properties()
        properties.loadFromXML(resource.inputStream)
    }

    fun getProperty(key: String): String? {
        val message = properties.getProperty(key) ?: return null
        val joiner = StringJoiner("\n")
        message.trim().split("\n").forEach { joiner.add(it.trim()) }
        return EmojiParser.parseToUnicode(joiner.toString())
    }

    fun getProperty(key: String, vararg arguments: Any): String? {
        val message = getProperty(key) ?: return null
        return MessageFormat.format(message, *arguments)
    }

    fun createWrapper(prefix: String): MessageProperties = Wrapper(this, prefix)

    private class Wrapper(
                    private val telegramProperties: MessageProperties,
                    private val prefix: String
            ) : MessageProperties() {

        override fun getProperty(key: String): String? {
            val message = telegramProperties.getProperty("$prefix.$key")
            return message ?: telegramProperties.getProperty(key)
        }
    }
}