package com.munoon.counter.messages

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface MessageRepository : JpaRepository<Message, Long> {
    fun findAllByDate(date: LocalDate): List<Message>
}