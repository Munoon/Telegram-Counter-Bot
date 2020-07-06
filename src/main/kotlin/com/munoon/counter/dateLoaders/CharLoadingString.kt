package com.munoon.counter.dateLoaders

import com.munoon.counter.configuration.DatesSettings
import com.munoon.counter.utils.MessageProperties
import com.vdurmont.emoji.EmojiParser
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

abstract class CharLoadingString(
        datesSettings: DatesSettings,
        messageProperties: MessageProperties
) : DateLoader(datesSettings, messageProperties) {
    final override fun getLoadingString(percent: Double): String {
        val progress = (percent / 2).toInt()
        val result = Array(50) {
            when {
                it < progress -> getProgressChar()
                it == progress -> getCurrentProgressChar()
                else -> getEmptyChar()
            }
        }.joinToString("")
        return addBorders(result)
    }

    fun addBorders(body: String): String = "[$body]"
    fun getEmptyChar(): String = " "

    abstract fun getProgressChar(): String
    abstract fun getCurrentProgressChar(): String
}

class SimpleCharLoadingString(
        datesSettings: DatesSettings,
        messageProperties: MessageProperties
) : CharLoadingString(datesSettings, messageProperties) {
    override fun getProgressChar(): String = "="
    override fun getCurrentProgressChar(): String = ">"
}

class EmojiLoadingString(
        datesSettings: DatesSettings,
        messageProperties: MessageProperties,
        environment: Environment
) : CharLoadingString(datesSettings, messageProperties) {
    private val progressChar: String
            = environment.getProperty("loadingString.emoji.progress")
            ?: throw java.lang.IllegalArgumentException("loadingString.emoji.progress not provided")

    private val currentProgressChar: String
            = environment.getProperty("loadingString.emoji.currentProgress")
            ?: throw java.lang.IllegalArgumentException("loadingString.emoji.currentProgress not provided")

    override fun getProgressChar(): String = progressChar
    override fun getCurrentProgressChar(): String = currentProgressChar

    override fun addBorders(body: String): String = EmojiParser.parseToUnicode("[$body]")
}