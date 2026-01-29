package com.example.fintech.api.controller

import com.example.fintech.api.dto.*
import com.example.fintech.entity.AccountEntity
import com.example.fintech.entity.TransactionEntity
import com.example.fintech.service.AccountsService
import com.example.fintech.service.TransactionsService
import com.example.fintech.service.TransfersService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class AccountsController(
    private val accountsService: AccountsService,
    private val transfersService: TransfersService,
    private val transactionsService: TransactionsService
) {

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    fun createAccount(@RequestBody @Valid req: CreateAccountRequest): AccountResponse {
        val acc = accountsService.createAccount(req.customerId, req.currency)
        return acc.toResponse()
    }

    @GetMapping("/accounts/{accountId}")
    fun getAccount(@PathVariable accountId: UUID): AccountResponse =
        accountsService.getAccount(accountId).toResponse()

    @PostMapping("/accounts/{accountId}/topups")
    @ResponseStatus(HttpStatus.CREATED)
    fun topUp(
        @PathVariable accountId: UUID,
        @RequestHeader("Idempotency-Key") idempotencyKey: String,
        @RequestBody @Valid req: TopUpRequest
    ): CreateOperationResponse {
        val txn = transfersService.topUp(accountId, idempotencyKey, req.amount)
        return CreateOperationResponse(txn.id, txn.status)
    }

    @GetMapping("/accounts/{accountId}/transactions")
    fun listTransactions(
        @PathVariable accountId: UUID,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): PagedResponse<TransactionShortResponse> {
        val safeLimit = limit.coerceIn(1, 200)
        val safeOffset = offset.coerceAtLeast(0)

        val items = transactionsService
            .listForAccount(accountId, safeLimit, safeOffset)
            .map { it.toShortResponse() }

        return PagedResponse(items, safeLimit, safeOffset)
    }

    private fun AccountEntity.toResponse() =
        AccountResponse(
            accountId = id,
            customerId = customerId,
            currency = currency,
            status = status,
            balance = balance.setScale(4).toPlainString()
        )

    private fun TransactionEntity.toShortResponse() =
        TransactionShortResponse(
            transactionId = id,
            type = type,
            amount = amount.setScale(4).toPlainString(),
            currency = currency,
            status = status,
            createdAt = createdAt.toString()
        )
}
