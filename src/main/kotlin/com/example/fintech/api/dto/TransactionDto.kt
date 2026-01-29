package com.example.fintech.api.dto

import com.example.fintech.domain.TransactionStatus
import com.example.fintech.domain.TransactionType
import java.util.*

data class TransactionShortResponse(
    val transactionId: UUID,
    val type: TransactionType,
    val amount: String,
    val currency: String,
    val status: TransactionStatus,
    val createdAt: String
)

data class TransactionDetailsResponse(
    val transactionId: UUID,
    val type: TransactionType,
    val idempotencyKey: String,
    val fromAccountId: UUID?,
    val toAccountId: UUID,
    val amount: String,
    val currency: String,
    val status: TransactionStatus,
    val failureReason: String?,
    val createdAt: String
)

data class PagedResponse<T>(
    val items: List<T>,
    val limit: Int,
    val offset: Int
)