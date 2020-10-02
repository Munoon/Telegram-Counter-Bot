package com.munoon.counter.rates

import com.munoon.counter.user.UserRepository
import com.munoon.counter.utils.NotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class RatesService(
        private val repository: RateRepository,
        private val userRepository: UserRepository
) {
    fun getOwnRates(userId: String): List<Rate> {
        return repository.getAllByUserId(userId)
    }

    fun getLastRate(telegramId: String): Rate {
        val user = userRepository.getByTelegramChatId(telegramId)
        val request = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "date"))
        return repository.findAllByUserId(user.id.toString(), request)
                .getOptional(0)
                .orElseThrow { NotFoundException("Rate with user telegram id $telegramId is not found") }
    }

    fun save(rate: Rate) = repository.save(rate)
}

fun <T> List<T>.getOptional(index: Int): Optional<T> = Optional.ofNullable(get(index))