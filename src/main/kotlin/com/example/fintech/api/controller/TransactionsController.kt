package com.example.fintech.api.controller

import com.example.fintech.api.dto.TransactionDetailsResponse
import com.example.fintech.service.TransactionsService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/transactions")
class TransactionsController(
    private val transactionsService: TransactionsService
) {

    @GetMapping("/{transactionId}")
    fun get(@PathVariable transactionId: UUID): TransactionDetailsResponse {
        val t = transactionsService.get(transactionId)
        return TransactionDetailsResponse(
            transactionId = t.id,
            type = t.type,
            idempotencyKey = t.idempotencyKey,
            fromAccountId = t.fromAccountId,
            toAccountId = t.toAccountId,
            amount = t.amount.setScale(4).toPlainString(),
            currency = t.currency,
            status = t.status,
            failureReason = t.failureReason,
            createdAt = t.createdAt.toString()
        )
    }
}
