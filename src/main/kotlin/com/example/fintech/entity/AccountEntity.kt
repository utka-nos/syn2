package com.example.fintech.entity

import com.example.fintech.domain.AccountStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "account",
    indexes = [
        Index(name = "ix_account_customer", columnList = "customerId"),
        Index(name = "ix_account_currency", columnList = "currency")
    ]
)
class AccountEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val customerId: UUID,

    @Column(nullable = false, length = 3)
    val currency: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: AccountStatus = AccountStatus.ACTIVE,

    @Column(nullable = false, precision = 19, scale = 4)
    var balance: BigDecimal = BigDecimal.ZERO,

    @Version
    var version: Long = 0,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)