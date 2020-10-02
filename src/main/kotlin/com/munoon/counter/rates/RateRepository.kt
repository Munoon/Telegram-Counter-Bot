package com.munoon.counter.rates

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface RateRepository: JpaRepository<Rate, Int> {
    fun findAllByUserId(userId: String, request: Pageable): List<Rate>

    fun getAllByUserId(userId: String): List<Rate>
}