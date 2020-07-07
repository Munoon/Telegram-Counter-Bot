package com.munoon.counter.rates

import org.springframework.stereotype.Service

@Service
class RatesService(private val repository: RateRepository) {
    fun getOwnRates(userId: String): List<Rate> {
        return repository.getAllByUserId(userId)
    }
}