package com.munoon.counter.utils

object TelegramUtils {
    private const val TEXT_LIMIT = 4096

    fun splitMessageText(text: String): List<String> {
        if (text.length < TEXT_LIMIT) {
            return listOf(text)
        }

        val result = ArrayList<String>()

        fun addPartAndReturnResult(text: String): String {
            result += text.substring(0, TEXT_LIMIT)
            return text.substring(TEXT_LIMIT)
        }

        var textToSend = text
        while (textToSend.length > 4096) {
            textToSend = addPartAndReturnResult(textToSend)
        }
        if (textToSend.isNotEmpty()) {
            result += textToSend
        }

        return result
    }
}