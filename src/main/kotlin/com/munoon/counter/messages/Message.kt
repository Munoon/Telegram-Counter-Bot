package com.munoon.counter.messages

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "messages")
data class Message(
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Int,

    @Column(name = "user_Id", nullable = false)
    val userId: String,

    @Column(name = "message", nullable = false)
    val message: String,

    @Column(name = "date", nullable = false)
    val date: LocalDate
)