package com.example.fintech.service

import com.example.fintech.entity.TransactionEntity
import com.example.fintech.repo.TransactionRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class TransactionsService(
    private val txns: TransactionRepository
) {
    fun get(transactionId: UUID): TransactionEntity =
        txns.findById(transactionId).orElseThrow { NotFoundException("Transaction $transactionId not found") }

    fun findByIdempotencyKey(key: String): TransactionEntity? =
        txns.findByIdempotencyKey(key)

    fun listForAccount(accountId: UUID, limit: Int, offset: Int): List<TransactionEntity> {
        val page = PageRequest.of(offset / limit, limit)
        return txns.findByToAccountIdOrFromAccountIdOrderByCreatedAtDesc(accountId, accountId, page).content
    }
}
