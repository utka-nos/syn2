package com.example.fintech.repo

import com.example.fintech.entity.TransactionEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface TransactionRepository : JpaRepository<TransactionEntity, UUID> {
    fun findByIdempotencyKey(idempotencyKey: String): TransactionEntity?

    fun findByToAccountIdOrFromAccountIdOrderByCreatedAtDesc(
        toAccountId: UUID,
        fromAccountId: UUID,
        pageable: Pageable
    ): Page<TransactionEntity>
}
