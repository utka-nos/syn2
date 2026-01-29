package com.example.fintech.entity

import com.example.fintech.domain.TransactionStatus
import com.example.fintech.domain.TransactionType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "txn",
    indexes = [
        Index(name = "ix_txn_to_account", columnList = "toAccountId"),
        Index(name = "ix_txn_from_account", columnList = "fromAccountId"),
        Index(name = "ix_txn_created_at", columnList = "createdAt")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "ux_txn_idempotency_key", columnNames = ["idempotencyKey"])
    ]
)
class TransactionEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: TransactionType,

    @Column(nullable = false, length = 128)
    val idempotencyKey: String,

    @Column
    val fromAccountId: UUID? = null,

    @Column(nullable = false)
    val toAccountId: UUID,

    @Column(nullable = false, precision = 19, scale = 4)
    val amount: BigDecimal,

    @Column(nullable = false, length = 3)
    val currency: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TransactionStatus,

    @Column
    var failureReason: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
