package com.munoon.counter.utils

import com.munoon.counter.rates.Rate
import com.vdurmont.emoji.EmojiParser
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors.toList

object RateUtil {
    fun printOwnRatesList(rates: List<Rate>): String {
        val dates = rates.stream().map { it.date }.sorted()
        return dates.map {
            val date = it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            val marksWithComments = rates
                    .filter { rate -> rate.date == it }
                    .joinToString("", transform = RateUtil::getMarksAndComment)
            "$date\n$marksWithComments"
        }
                .collect(toList())
                .joinToString("\n\n")
                .let(EmojiParser::parseToUnicode)
    }

    fun getMarksAndComment(rate: Rate) = getMarksAndComment(rate.marks, rate.comment)

    fun getMarksAndComment(marks: List<String>?, additionalMessage: String?) =
            if (!marks.isNullOrEmpty() && !additionalMessage.isNullOrBlank())
                "${marks.joinToString("")} — $additionalMessage"
            else if (!marks.isNullOrEmpty()) marks.joinToString("")
            else if (!additionalMessage.isNullOrBlank()) additionalMessage
            else ""
}