package com.munoon.counter.rates

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.*

interface RateRepository: JpaRepository<Rate, Int> {
    @Query("FROM Rate WHERE userId = ?1 AND date = ?2")
    fun getRateByDate(userId: String, date: LocalDate): Optional<Rate>
}