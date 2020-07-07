package com.munoon.counter.rates

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "rates")
data class Rate(
        @Id
        @GeneratedValue
        var id: Int?,

        var userId: String,

        var messageId: String,

        @CollectionTable(name = "rates_marks", joinColumns = [JoinColumn(name = "rate_id")])
        @Column(name = "mark")
        @ElementCollection(fetch = FetchType.EAGER)
        var marks: MutableList<String>?,

        var comment: String?,

        var marking: Boolean,

        var date: LocalDate = LocalDate.now()
)