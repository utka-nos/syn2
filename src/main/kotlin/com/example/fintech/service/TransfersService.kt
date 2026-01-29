package com.example.fintech.service

import com.example.fintech.domain.AccountStatus
import com.example.fintech.domain.TransactionStatus
import com.example.fintech.domain.TransactionType
import com.example.fintech.entity.TransactionEntity
import com.example.fintech.repo.AccountRepository
import com.example.fintech.repo.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class TransfersService(
    private val accounts: AccountRepository,
    private val txns: TransactionRepository
) {

    @Transactional
    fun topUp(accountId: UUID, idempotencyKey: String, amount: BigDecimal): TransactionEntity {
        validateIdempotencyKey(idempotencyKey)
        validateAmount(amount)

        // Idempotency: if exists -> return same transaction
        txns.findByIdempotencyKey(idempotencyKey)?.let { return it }

        val acc = accounts.findByIdForUpdate(accountId)
            ?: throw NotFoundException("Account $accountId not found")

        if (acc.status != AccountStatus.ACTIVE) throw UnprocessableException("Account $accountId is not ACTIVE")

        acc.balance = acc.balance.add(amount)

        val txn = TransactionEntity(
            type = TransactionType.TOP_UP,
            idempotencyKey = idempotencyKey,
            fromAccountId = null,
            toAccountId = acc.id,
            amount = amount,
            currency = acc.currency,
            status = TransactionStatus.POSTED
        )
        return txns.save(txn)
    }

    @Transactional
    fun transfer(fromAccountId: UUID, toAccountId: UUID, idempotencyKey: String, amount: BigDecimal): TransactionEntity {
        validateIdempotencyKey(idempotencyKey)
        validateAmount(amount)
        if (fromAccountId == toAccountId) throw BadRequestException("fromAccountId must differ from toAccountId")

        // Idempotency: if exists -> return same transaction
        txns.findByIdempotencyKey(idempotencyKey)?.let { return it }

        // To avoid deadlocks: lock accounts in deterministic order
        val (firstId, secondId) = if (fromAccountId.toString() <= toAccountId.toString())
            fromAccountId to toAccountId else toAccountId to fromAccountId

        val first = accounts.findByIdForUpdate(firstId) ?: throw NotFoundException("Account $firstId not found")
        val second = accounts.findByIdForUpdate(secondId) ?: throw NotFoundException("Account $secondId not found")

        val from = if (first.id == fromAccountId) first else second
        val to = if (first.id == toAccountId) first else second

        if (from.status != AccountStatus.ACTIVE) throw UnprocessableException("From account is not ACTIVE")
        if (to.status != AccountStatus.ACTIVE) throw UnprocessableException("To account is not ACTIVE")

        if (from.currency != to.currency) throw BadRequestException("Accounts currency mismatch: ${from.currency} vs ${to.currency}")

        if (from.balance < amount) throw UnprocessableException("Insufficient funds")

        from.balance = from.balance.subtract(amount)
        to.balance = to.balance.add(amount)

        val txn = TransactionEntity(
            type = TransactionType.TRANSFER,
            idempotencyKey = idempotencyKey,
            fromAccountId = from.id,
            toAccountId = to.id,
            amount = amount,
            currency = from.currency,
            status = TransactionStatus.POSTED
        )
        return txns.save(txn)
    }

    private fun validateAmount(amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) throw BadRequestException("Amount must be > 0")
    }

    private fun validateIdempotencyKey(key: String) {
        val k = key.trim()
        if (k.isEmpty()) throw BadRequestException("Idempotency-Key is required")
        if (k.length > 128) throw BadRequestException("Idempotency-Key too long (max 128)")
    }
}
