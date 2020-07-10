package com.munoon.counter.rates

import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.NotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RatesService(
        private val repository: RateRepository,
        private val userRepository: UserRepository
) {
    fun getOwnRates(userId: String): List<Rate> {
        return repository.getAllByUserId(userId)
    }

    fun getRateByTelegramId(telegramId: String, date: LocalDate): Rate {
        val user = userRepository.getByTelegramChatId(telegramId)
        return repository.getRateByDate(user.id.toString(), date)
                .orElseThrow { NotFoundException("Rate with user telegram id $telegramId and date $date is not found") }
    }

    fun save(rate: Rate) = repository.save(rate)
}